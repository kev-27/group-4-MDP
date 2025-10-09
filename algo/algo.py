import heapq
import math
from typing import List
import numpy as np
from entities.Robot import Robot
from entities.Entity import Obstacle, CellState, Grid
from consts import Direction, MOVE_DIRECTION, TURN_FACTOR, ITERATIONS, TURN_RADIUS, SAFE_COST
from python_tsp.exact import solve_tsp_dynamic_programming

turn_wrt_big_turns = [[3 * TURN_RADIUS, TURN_RADIUS],
                  [4 * TURN_RADIUS, 2 * TURN_RADIUS]]


class MazeSolver:
    def __init__(
            self,
            size_x: int,
            size_y: int,
            robot_x: int,
            robot_y: int,
            robot_direction: Direction,
            big_turn=None # the big_turn here is to allow 3-1 turn(0 - by default) | 4-2 turn(1)
    ):
        # Initialize a Grid object for the arena representation
        self.grid = Grid(size_x, size_y)
        # Initialize a Robot object for robot representation
        self.robot = Robot(robot_x, robot_y, robot_direction)
        # Create tables for paths and costs
        self.path_table = dict()
        self.cost_table = dict()
        if big_turn is None:
            self.big_turn = 0
        else:
            self.big_turn = int(big_turn)
    def _id_to_coord_map(self):
        return {ob.obstacle_id: (ob.x, ob.y) for ob in self.grid.obstacles}

    @staticmethod
    def _dir_to_delta(d):
        """Direction -> (dx, dy) for the front/marker cell."""
        if d == Direction.NORTH: return (0, 1)
        if d == Direction.EAST:  return (1, 0)
        if d == Direction.SOUTH: return (0,-1)
        if d == Direction.WEST:  return (-1,0)
        return (0, 0)

    def extract_visit_order(self, optimal_path):
        """
        Walks the chosen optimal_path and returns a list of (obstacle_id, x, y)
        in the exact order obstacles are first scanned.
        """
        id2coord = self._id_to_coord_map()
        seen_ids = set()
        seen_coords = set()
        order = []  # [(id, x, y), ...]

        for st in (optimal_path or []):
            # Preferred: explicit id on the state (either 'screenshot_id' or 's')
            sid = getattr(st, "screenshot_id", None)
            if sid is None:
                sid = getattr(st, "s", None)  # some serializers put it here

            if isinstance(sid, int) and sid != -1:
                if sid not in seen_ids:
                    xy = id2coord.get(sid)
                    if xy is not None:
                        order.append((sid, xy[0], xy[1]))
                    else:
                        # id unknown -> append id with None coords
                        order.append((sid, None, None))
                    seen_ids.add(sid)
                continue

            # Fallback: infer by looking at the front cell and matching to an obstacle
            # Requires the state to carry x,y,direction
            dx, dy = self._dir_to_delta(getattr(st, "direction", None))
            ox = getattr(st, "x", None)
            oy = getattr(st, "y", None)
            if ox is None or oy is None:  # can't infer
                continue
            ox += dx
            oy += dy

            # Match front cell to an obstacle coordinate
            for ob in self.grid.obstacles:
                if ob.x == ox and ob.y == oy:
                    key = (ob.obstacle_id, ox, oy)
                    if key not in seen_coords and ob.obstacle_id not in seen_ids:
                        order.append((ob.obstacle_id, ox, oy))
                        seen_coords.add(key)
                        seen_ids.add(ob.obstacle_id)
                    break

        return order  # e.g. [(3, 5, 9), (7, 12, 2), ...]

    def add_obstacle(self, x: int, y: int, direction: Direction, obstacle_id: int):
        """Add obstacle to MazeSolver object

        Args:
            x (int): x coordinate of obstacle
            y (int): y coordinate of obstacle
            direction (Direction): Direction of obstacle
            obstacle_id (int): ID of obstacle
        """
        # Create an obstacle object
        obstacle = Obstacle(x, y, direction, obstacle_id)
        # Add created obstacle to grid object
        self.grid.add_obstacle(obstacle)

    def reset_obstacles(self):
        self.grid.reset_obstacles()

    @staticmethod
    def compute_coord_distance(x1: int, y1: int, x2: int, y2: int, level=1):
        """Compute the L-n distance between two coordinates

        Args:
            x1 (int)
            y1 (int)
            x2 (int)
            y2 (int)
            level (int, optional): L-n distance to compute. Defaults to 1.

        Returns:
            float: L-n distance between the two given points
        """
        horizontal_distance = x1 - x2
        vertical_distance = y1 - y2

        # Euclidean distance
        if level == 2:
            return math.sqrt(horizontal_distance ** 2 + vertical_distance ** 2)

        return abs(horizontal_distance) + abs(vertical_distance)

    @staticmethod
    def compute_state_distance(start_state: CellState, end_state: CellState, level=1):
        """Compute the L-n distance between two cell states

        Args:
            start_state (CellState): Start cell state
            end_state (CellState): End cell state
            level (int, optional): L-n distance to compute. Defaults to 1.

        Returns:
            float: L-n distance between the two given cell states
        """
        return MazeSolver.compute_coord_distance(start_state.x, start_state.y, end_state.x, end_state.y, level)

    @staticmethod
    def get_visit_options(n):
        """Generate all possible n-digit binary strings

        Args:
            n (int): number of digits in binary string to generate

        Returns:
            List: list of all possible n-digit binary strings
        """
        s = []
        l = bin(2 ** n - 1).count('1')

        for i in range(2 ** n):
            s.append(bin(i)[2:].zfill(l))

        s.sort(key=lambda x: x.count('1'), reverse=True)
        return s

    def get_optimal_order_dp(self, retrying) -> List[CellState]:
        distance = 1e9
        optimal_path = []

        #print(f"Inside get_optimal_order_dp: retrying = {retrying}")
        # Get all possible positions that can view the obstacles
        all_view_positions = self.grid.get_view_obstacle_positions(retrying)
        #print(all_view_positions)
        #print(f"all_view_positions: {all_view_positions}")
        #print(f"All view position: {all_view_positions}")
        for op in self.get_visit_options(len(all_view_positions)):
            # op is binary string of length len(all_view_positions) == len(obstacles)
            #TLDR OP IS EACH PERMUTATION OF SIMPLY VISITING DIFFERENT OBSTACLES, NOT ACCOUNTING FOR VIEWS
            # Initialize `items` to be a list containing the robot's start state as the first item
            items = [self.robot.get_start_state()]
            # Initialize `cur_view_positions` to be an empty list
            cur_view_positions = [] #RESET FOR EACH OP COMBI OF VISITING
            cur_obstacle_ids = [] #EMPTY OBSTACLE ID ORDER
            # print(f"===================\nop = {op}")
            # print("List of obstacle visited: \n")
            
            # For each obstacle
            for idx in range(len(all_view_positions)):
                # If robot is visiting, MEANING 1 IN THE BINARY STRING
                if op[idx] == '1':
                    # Add possible cells to `items`
                    items = items + all_view_positions[idx]
                    # Add possible cells to `cur_view_positions`
                    cur_view_positions.append(all_view_positions[idx])
                    #print("obstacle: {}\n".format(self.grid.obstacles[idx]))
            #ITEMS IS START POINT + EVERY POSSIBLE VIEW STATE
            # Generate the path cost for the items
            self.path_cost_generator(items) #COMPUTES AND STORES ALL PAIR DISTANCES FROM ROBOT START TO EACH VIEW, STORING THE PATH AND COST
            combination = []
            self.generate_combination(cur_view_positions, 0, [], combination, [ITERATIONS])
            #TLDR THIS CREATES ALL COMBINATIONS OF VISITING DIFFERENT VIEWS FOR EACH OBSTACLE
            # E.G. A1,B2,C3 OR B3,A2,C1
            origin = [items[0]]
            for c in combination: # ITERATES THROUGH EACH COMBINATION
                visited_candidates = [0] # add the start state of the robot
                
                cur_index = 1 #THIS TRACKS THE INDEX OF EACH PARTICULAR VIEW IN THE ITEMS LIST
                fixed_cost = 0 # the cost applying for the position taking obstacle pictures
                for index, view_position in enumerate(cur_view_positions):
                    choice = c[index]
                    if choice is None:
                        # Skip unreachable obstacles
                        cur_index += len(view_position)  # still advance index for next obstacle
                        continue
                    visited_candidates.append(cur_index + choice)
                    fixed_cost += view_position[choice].penalty
                    cur_index += len(view_position)

                cost_np = np.zeros((len(visited_candidates), len(visited_candidates)))

                for s in range(len(visited_candidates) - 1):
                    for e in range(s + 1, len(visited_candidates)):
                        u = items[visited_candidates[s]] #REMINDER ITEMS ARE DIFFERENT VIEWS
                        v = items[visited_candidates[e]]
                        if (u, v) in self.cost_table.keys():
                            cost_np[s][e] = self.cost_table[(u, v)]
                        else:
                            cost_np[s][e] = 1e9
                        if (v, u) in self.cost_table:
                            cost_np[e][s] = self.cost_table[(v, u)]
                        else:
                            cost_np[e][s] = 1e9 
                #TLDR THIS ADDS THE COST OF EACH PAIR OF OBJECTS E.G. A1 -> B2 OR C3-> A2
                cost_np[:, 0] = 0
                if np.any(cost_np == 1e9):
                    continue
                _permutation, _distance = solve_tsp_dynamic_programming(cost_np)
                # print(f"fixed_cost = {fixed_cost}")
                # print(f"distance = {_distance}")
                if _distance + fixed_cost >= distance:
                    continue

                valid_combination = True
                optimal_path = [items[0]]
                partial_path = [items[0]]
                distance = _distance + fixed_cost
                #START FROM ROBOT'S STATE NODE , THEN ADD THE OBSTACLES IN ORDER
                for i in range(len(_permutation) - 1):
                    from_item = items[visited_candidates[_permutation[i]]]
                    to_item = items[visited_candidates[_permutation[i + 1]]]
                    if (from_item, to_item) not in self.path_table or self.path_table[(from_item, to_item)] == 0:
                        valid_combination = False
                        break
                    cur_path = self.path_table[(from_item, to_item)]
                    #print(cur_path)
                    for j in range(1, len(cur_path)): #ADD EACH OBSTACLES X,Y,DIRECTION TO PATH
                        partial_path.append(CellState(cur_path[j][0], cur_path[j][1], cur_path[j][2]))

                    partial_path[-1].set_screenshot(to_item.screenshot_id)
                if valid_combination:
                    optimal_path = partial_path

            if optimal_path is not None and len(optimal_path) > 0 and optimal_path != origin:
                break
            
        if optimal_path is None or len(optimal_path) == 0:
            #SAFETYNET TO RETURN JUST START POINT IF SOMEHOW NILL LIST
            optimal_path = [items[0]]
        return optimal_path, distance

    @staticmethod
    def generate_combination(view_positions, index, current, result, iteration_left):
        if index == len(view_positions):
            result.append(current[:])
            return

        if iteration_left[0] == 0:
            return

        iteration_left[0] -= 1
        if not view_positions[index]:  # EMPTY list, skip this obstacle
        # Append a placeholder (e.g., None) to maintain indexing
            current.append(None)
            MazeSolver.generate_combination(view_positions, index + 1, current, result, iteration_left)
            current.pop()
        else:
            for j in range(len(view_positions[index])):
                current.append(j)
                MazeSolver.generate_combination(view_positions, index + 1, current, result, iteration_left)
                current.pop()

    def get_safe_cost(self, x, y):
        """Get the safe cost of a particular x,y coordinate wrt obstacles that are exactly 2 units away from it in both x and y directions

        Args:
            x (int): x-coordinate
            y (int): y-coordinate

        Returns:
            int: safe cost
        """
        for ob in self.grid.obstacles:
            if abs(ob.x-x) == 2 and abs(ob.y-y) == 2:
                return SAFE_COST
            
            if abs(ob.x-x) == 1 and abs(ob.y-y) == 2:
                return SAFE_COST
            
            if abs(ob.x-x) == 2 and abs(ob.y-y) == 1:
                return SAFE_COST

        return 0

    def get_neighbors(self, x, y, direction):  # TODO: see the behavior of the robot and adjust...
        """
        Return a list of tuples with format:
        newX, newY, new_direction
        """
        # Neighbors have the following format: {newX, newY, movement direction, safe cost}
        # Neighbors are coordinates that fulfill the following criteria:
        # If moving in the same direction:
        #   - Valid position within bounds
        #   - Must be at least 4 units away in total (x+y) 
        #   - Furthest distance must be at least 3 units away (x or y)
        # If it is exactly 2 units away in both x and y directions, safe cost = SAFECOST. Else, safe cost = 0

        neighbors = []
        # Assume that after following this direction, the car direction is EXACTLY md
        for dx, dy, md in MOVE_DIRECTION:
            if md == direction:  # if the new direction == md, MEANING GO STRAIGHT
                # Check for valid position
                #ALL MOVEMENTS ARE JUST 1 UNIT IN X OR Y DIRECTION
                if self.grid.reachable(x + dx, y + dy):  # go forward;
                    # Get safe cost of destination
                    safe_cost = self.get_safe_cost(x + dx, y + dy)
                    neighbors.append((x + dx, y + dy, md, safe_cost))
                # Check for valid position
                if self.grid.reachable(x - dx, y - dy):  # go back;
                    # Get safe cost of destination
                    safe_cost = self.get_safe_cost(x - dx, y - dy)
                    neighbors.append((x - dx, y - dy, md, safe_cost))

            else:  # consider 8 cases
                #TO ADJUST, WE MUST SEE HOW OUR ROBOT TURNS LEFT/RIGHT AND CORRESPONDING REVERSE TURN
                #TLDR CONIDER EITHER TURING TOWARD THE DIRECTION OR REVERSING TO END UP FACING DIRECTION
                # Turning displacement is either 4-2 or 3-1
                bigger_change = turn_wrt_big_turns[self.big_turn][0]
                smaller_change = turn_wrt_big_turns[self.big_turn][1]
                #FOR TESTING FIXED TURNS 6,2
                bigger_changeL = 4 #left turn
                smaller_changeL = 2
                bigger_changeR = 4 #right turn
                smaller_changeR = 3
                bigger_changeBL = 3 #y coord
                smaller_changeBL = 3
                bigger_changeBR = 4 #y coord
                smaller_changeBR = 3
                # north <-> east
                if direction == Direction.NORTH and md == Direction.EAST:

                    # Check for valid position
                    if self.grid.reachable(x + bigger_changeR, y + smaller_changeR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        # Get safe cost of destination
                        safe_cost = self.get_safe_cost(x + bigger_changeR, y + smaller_changeR)
                        #if x==11 and y==8:
                            #print("adding this particular path, ", x+bigger_change2, y+smaller_change2, "from",x,y, "North to east")
                        neighbors.append((x + bigger_changeR, y + smaller_changeR, md, safe_cost + 10))

                    # Check for valid position
                    if self.grid.reachable(x - smaller_changeBL, y - bigger_changeBL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        # Get safe cost of destination
                        safe_cost = self.get_safe_cost(x - smaller_changeBL, y - bigger_changeBL)
                        neighbors.append((x - smaller_changeBL, y - bigger_changeBL, md, safe_cost + 10))

                if direction == Direction.EAST and md == Direction.NORTH:
                    if self.grid.reachable(x + smaller_changeL, y + bigger_changeL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        safe_cost = self.get_safe_cost(x + smaller_changeL, y + bigger_changeL)
                        #if x==11 and y==8:
                         #   print("adding this particular path, ", x+smaller_change, y+bigger_change, "from",x,y, "east to north")
                        neighbors.append((x + smaller_changeL, y + bigger_changeL, md, safe_cost + 10))

                    if self.grid.reachable(x - bigger_changeBR, y - smaller_changeBR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        safe_cost = self.get_safe_cost(x - bigger_changeBR, y - smaller_changeBR)
                        neighbors.append((x - bigger_changeBR, y - smaller_changeBR, md, safe_cost + 10))

                # east <-> south
                if direction == Direction.EAST and md == Direction.SOUTH:
                    
                    if self.grid.reachable(x + smaller_changeR, y - bigger_changeR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        safe_cost = self.get_safe_cost(x + smaller_changeR, y - bigger_changeR)
                        #if x==11 and y==8:
                        #print("adding this particular path, ", x+smaller_change2, y-bigger_change2, "from",x,y, "east to south")
                        neighbors.append((x + smaller_changeR, y - bigger_changeR, md, safe_cost + 10))

                    if self.grid.reachable(x - bigger_changeBL, y + smaller_changeBL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        safe_cost = self.get_safe_cost(x - bigger_changeBL, y + smaller_changeBL)
                        neighbors.append((x - bigger_changeBL, y + smaller_changeBL, md, safe_cost + 10))

                if direction == Direction.SOUTH and md == Direction.EAST:
                    if self.grid.reachable(x + bigger_changeL, y - smaller_changeL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        safe_cost = self.get_safe_cost(x + bigger_changeL, y - smaller_changeL)
                        #if x==11 and y==8:
                        #print("adding this particular path, ", x+bigger_change, y-smaller_change, "from",x,y,"south to east")
                        neighbors.append((x + bigger_changeL, y - smaller_changeL, md, safe_cost + 10))

                    if self.grid.reachable(x - smaller_changeBR, y + bigger_changeBR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        safe_cost = self.get_safe_cost(x - smaller_changeBR, y + bigger_changeBR)
                        neighbors.append((x - smaller_changeBR, y + bigger_changeBR, md, safe_cost + 10))

                # south <-> west
                if direction == Direction.SOUTH and md == Direction.WEST:
                    if self.grid.reachable(x - bigger_changeR, y - smaller_changeR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        safe_cost = self.get_safe_cost(x - bigger_changeR, y - smaller_changeR)
                        #if x==11 and y==8:
                        #print("adding this particular path, ", x-bigger_change2, y-smaller_change2, "from",x,y,"south to west")
                        neighbors.append((x - bigger_changeR, y - smaller_changeR, md, safe_cost + 10))

                    if self.grid.reachable(x + smaller_changeBL, y + bigger_changeBL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        safe_cost = self.get_safe_cost(x + smaller_changeBL, y + bigger_change)
                        neighbors.append((x + smaller_changeBL, y + bigger_changeBL, md, safe_cost + 10))

                if direction == Direction.WEST and md == Direction.SOUTH:
                    if self.grid.reachable(x - smaller_changeL, y - bigger_changeL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        safe_cost = self.get_safe_cost(x - smaller_changeL, y - bigger_changeL)
                        #if x==11 and y==8:
                        #print("adding this particular path, ", x-smaller_change, y-bigger_change, "from",x,y,"west to south")
                        neighbors.append((x - smaller_changeL, y - bigger_changeL, md, safe_cost + 10))

                    if self.grid.reachable(x + bigger_changeBR, y + smaller_changeBR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        safe_cost = self.get_safe_cost(x + bigger_changeBR, y + smaller_changeBR)
                        neighbors.append((x + bigger_changeBR, y + smaller_changeBR, md, safe_cost + 10))

                # west <-> north
                if direction == Direction.WEST and md == Direction.NORTH:
                    if self.grid.reachable(x - smaller_changeR, y + bigger_changeR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        safe_cost = self.get_safe_cost(x - smaller_changeR, y + bigger_changeR)
                        #if x==11 and y==8:
                        #print("adding this particular path, ", x-smaller_change2, y+bigger_change2, "from",x,y,"west to north")
                        neighbors.append((x - smaller_changeR, y + bigger_changeR, md, safe_cost + 10))

                    if self.grid.reachable(x + bigger_changeBL, y - smaller_changeBL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        safe_cost = self.get_safe_cost(x + bigger_changeBL, y - smaller_changeBL)
                        neighbors.append((x + bigger_changeBL, y - smaller_changeBL, md, safe_cost + 10))

                if direction == Direction.NORTH and md == Direction.WEST:
                    if self.grid.reachable(x + smaller_changeL, y - bigger_changeL, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction):
                        safe_cost = self.get_safe_cost(x + smaller_changeL, y - bigger_changeL)
                        #if x==11 and y==8:
                        #print("adding this particular path, ", x+smaller_change, y-bigger_change, "from",x,y,"north to west")
                        neighbors.append((x + smaller_changeL, y - bigger_changeL, md, safe_cost + 10))

                    if self.grid.reachable(x + smaller_changeBR, y - bigger_changeBR, turn = True) and self.grid.reachable(x, y, preTurn = True,direction = direction,back=True):
                        safe_cost = self.get_safe_cost(x + smaller_changeBR, y - bigger_changeBR)
                        neighbors.append((x + smaller_changeBR, y - bigger_changeBR, md, safe_cost + 10))

        return neighbors

    def path_cost_generator(self, states: List[CellState]):
        """Generate the path cost between the input states and update the tables accordingly

        Args:
            states (List[CellState]): cell states to visit
        """
        def record_path(start, end, parent: dict, cost: int):

            # Update cost table for the (start,end) and (end,start) edges
            self.cost_table[(start, end)] = cost

            path = []
            cursor = (end.x, end.y, end.direction)

            while cursor in parent: #RECONSTRUCTION OF PATH
                path.append(cursor) #STARTING WITH THE END
                cursor = parent[cursor] #THEN GOING TO THE NODE BEFORE

            path.append(cursor)

            # Update path table for the (start,end) and (end,start) edges, with the (start,end) edge being the reversed path
            #THE FULL PATH FROM ONE STATE TO ANOTHER GETS STORED
            self.path_table[(start, end)] = path[::-1]


        def astar_search(start: CellState, end: CellState):
            # astar search algo with three states: x, y, direction

            # If it is already done before, return
            if (start, end) in self.path_table:
                return

            # Heuristic to guide the search: 'distance' is calculated by f = g + h
            # g is the actual distance moved so far from the start node to current node
            # h is the heuristic distance from current node to end node
            g_distance = {(start.x, start.y, start.direction): 0}

            # format of each item in heap: (f_distance of node, x coord of node, y coord of node)
            # heap in Python is a min-heap
            heap = [(self.compute_state_distance(start, end), start.x, start.y, start.direction)]
            parent = dict()
            visited = set()

            while heap:
                # Pop the node with the smallest distance
                _, cur_x, cur_y, cur_direction = heapq.heappop(heap)
                
                if (cur_x, cur_y, cur_direction) in visited:
                    continue

                if end.is_eq(cur_x, cur_y, cur_direction): #IF GOAL REACHED
                    record_path(start, end, parent, g_distance[(cur_x, cur_y, cur_direction)])
                    return

                visited.add((cur_x, cur_y, cur_direction))
                cur_distance = g_distance[(cur_x, cur_y, cur_direction)]
                
                for next_x, next_y, new_direction, safe_cost in self.get_neighbors(cur_x, cur_y, cur_direction):
                #TLDR GOES THROUGH EACH POSSIBLE NEXT MOVEMENT SPOT FROM THE CURRENT SPOT (AFTER ACCOUNTING TURN RADIUS)
                    if (next_x, next_y, new_direction) in visited:
                        continue
                    #if next_x ==13 and next_y ==12:
                    #    print(cur_x,cur_y)
                    move_cost = Direction.rotation_cost(new_direction, cur_direction) * TURN_FACTOR + 1 + safe_cost

                    # the cost to check if any obstacles that considered too near the robot; if it
                    # safe_cost =

                    # new cost is calculated by the cost to reach current state + cost to move from
                    # current state to new state + heuristic cost from new state to end state
                    #CUR+MOVE IS G, COMPUTE_COORD DIST IS H
                    next_cost = cur_distance + move_cost + \
                                self.compute_coord_distance(next_x, next_y, end.x, end.y)
                    #ADD THE SELECTED NEIGHBOUR'S G INTO ARRAY, OR IF A LOWER G PATH TO THE SELECTED NEIGHBOUR IS FOUND REPLACE IT (AND PARENT TOO)
                    if (next_x, next_y, new_direction) not in g_distance or \
                            g_distance[(next_x, next_y, new_direction)] > cur_distance + move_cost:
                        
                        g_distance[(next_x, next_y, new_direction)] = cur_distance + move_cost
                        parent[(next_x, next_y, new_direction)] = (cur_x, cur_y, cur_direction) #TRACK PREV NODE IF NOT CUR NEIGHBOUR IS POINTLESS

                        heapq.heappush(heap, (next_cost, next_x, next_y, new_direction))

        # Nested loop through all the state pairings
        for i in range(len(states) - 1):
            #print(states[i])
            for j in range(i + 1, len(states)):
                astar_search(states[i], states[j])
                astar_search(states[j], states[i]) #CALCULATE REVERSE ASSYMETRIC PATH
                #print(self.path_table[(states[i], states[j])])
       # for (u, v), path in self.path_table.items():
        #    if (u.x, u.y) == (11,8) or (v.x, v.y) == (11,8):
         #       print(f"Path {u} -> {v}:")
          #      for p in path:
           #         print("   ", p)


if __name__ == "__main__":
    pass
