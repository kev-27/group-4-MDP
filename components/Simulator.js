import React from "react";
import { useState, useEffect } from "react";
import QueryAPI from "./QueryAPI";

const Direction = {
  NORTH: 0,
  EAST: 2,
  SOUTH: 4,
  WEST: 6,
  SKIP: 8,
};

const ObDirection = {
  NORTH: 0,
  EAST: 2,
  SOUTH: 4,
  WEST: 6,
  SKIP: 8,
};

const DirectionToString = {
  0: "Up",
  2: "Right",
  4: "Down",
  6: "Left",
  8: "None",
};

const transformCoord = (x, y) => {
  return { x: 19 - y, y: x };
};

function classNames(...classes) {
  return classes.filter(Boolean).join(" ");
}

const dirToRotationDeg = (d) => {
  if (Number(d) === Direction.NORTH) return 0;
  if (Number(d) === Direction.EAST) return 90;
  if (Number(d) === Direction.SOUTH) return 180;
  if (Number(d) === Direction.WEST) return 270;
  return 0;
};

export default function Simulator() {
  const [robotState, setRobotState] = useState({
    x: 1,
    y: 1,
    d: Direction.NORTH,
    s: -1,
  });
  const [robotX, setRobotX] = useState(1);
  const [robotY, setRobotY] = useState(1);
  const [robotDir, setRobotDir] = useState(0);
  const [obstacles, setObstacles] = useState([]);
  const [obXInput, setObXInput] = useState(0);
  const [obYInput, setObYInput] = useState(0);
  const [directionInput, setDirectionInput] = useState(ObDirection.NORTH);
  const [isComputing, setIsComputing] = useState(false);
  const [path, setPath] = useState([]);
  const [commands, setCommands] = useState([]);
  const [page, setPage] = useState(0);

  const generateNewID = () => {
    while (true) {
      let new_id = Math.floor(Math.random() * 10) + 1;
      let ok = true;
      for (const ob of obstacles) {
        if (ob.id === new_id) {
          ok = false;
          break;
        }
      }
      if (ok) return new_id;
    }
  };

  const generateRobotCells = () => {
    const robotCells = [];
    let markerX = 0;
    let markerY = 0;

    if (Number(robotState.d) === Direction.NORTH) markerY++;
    else if (Number(robotState.d) === Direction.EAST) markerX++;
    else if (Number(robotState.d) === Direction.SOUTH) markerY--;
    else if (Number(robotState.d) === Direction.WEST) markerX--;

    for (let i = -1; i < 2; i++) {
      for (let j = -1; j < 2; j++) {
        const coord = transformCoord(robotState.x + i, robotState.y + j);
        if (markerX === i && markerY === j) {
          robotCells.push({ x: coord.x, y: coord.y, d: robotState.d, s: robotState.s });
        } else {
          robotCells.push({ x: coord.x, y: coord.y, d: null, s: -1 });
        }
      }
    }
    return robotCells;
  };

  const onChangeX = (e) => {
    if (Number.isInteger(Number(e.target.value))) {
      const nb = Number(e.target.value);
      if (0 <= nb && nb < 20) return setObXInput(nb);
    }
    setObXInput(0);
  };

  const onChangeY = (e) => {
    if (Number.isInteger(Number(e.target.value))) {
      const nb = Number(e.target.value);
      if (0 <= nb && nb <= 19) return setObYInput(nb);
    }
    setObYInput(0);
  };

  const onChangeRobotX = (e) => {
    if (Number.isInteger(Number(e.target.value))) {
      const nb = Number(e.target.value);
      if (1 <= nb && nb < 19) return setRobotX(nb);
    }
    setRobotX(1);
  };

  const onChangeRobotY = (e) => {
    if (Number.isInteger(Number(e.target.value))) {
      const nb = Number(e.target.value);
      if (1 <= nb && nb < 19) return setRobotY(nb);
    }
    setRobotY(1);
  };

  const onClickObstacle = () => {
    if (!obXInput && !obYInput) return;
    setObstacles((prev) => [
      ...prev,
      { x: obXInput, y: obYInput, d: directionInput, id: generateNewID() },
    ]);
  };

  const onClickRobot = () => {
    setRobotState({ x: robotX, y: robotY, d: robotDir, s: -1 });
  };

  const onDirectionInputChange = (e) => setDirectionInput(Number(e.target.value));
  const onRobotDirectionInputChange = (e) => setRobotDir(e.target.value);

  const onRemoveObstacle = (ob) => {
    if (path.length > 0 || isComputing) return;
    setObstacles((prev) => prev.filter((o) => !(o.x === ob.x && o.y === ob.y)));
  };

  const compute = () => {
    setIsComputing(true);
    QueryAPI.query(obstacles, robotX, robotY, robotDir, (data, err) => {
      if (data) {
        setPath(data.data.path);
        const cmds = [];
        for (let x of data.data.commands) {
          if (!x.startsWith("SNAP")) cmds.push(x);
        }
        setCommands(cmds);
      }
      setIsComputing(false);
    });
  };

  const onResetAll = () => {
    setRobotX(1);
    setRobotDir(0);
    setRobotY(1);
    setRobotState({ x: 1, y: 1, d: Direction.NORTH, s: -1 });
    setPath([]);
    setCommands([]);
    setPage(0);
    setObstacles([]);
  };

  const onReset = () => {
    setRobotX(1);
    setRobotDir(0);
    setRobotY(1);
    setRobotState({ x: 1, y: 1, d: Direction.NORTH, s: -1 });
    setPath([]);
    setCommands([]);
    setPage(0);
  };

  const renderGrid = () => {
    const rows = [];
    const robotCells = generateRobotCells();
    const centerT = transformCoord(robotState.x, robotState.y);

    for (let i = 0; i < 20; i++) {
      const cells = [
        <td key={`hdr-${i}`} className="w-5 h-5 md:w-8 md:h-8">
          <span className="text-black font-bold text-[0.6rem] md:text-base ">{19 - i}</span>
        </td>,
      ];

      for (let j = 0; j < 20; j++) {
        let foundOb = null;
        let foundRobotCell = null;

        for (const ob of obstacles) {
          const t = transformCoord(ob.x, ob.y);
          if (t.x === i && t.y === j) {
            foundOb = ob;
            break;
          }
        }

        if (!foundOb) {
          for (const cell of robotCells) {
            if (cell.x === i && cell.y === j) {
              foundRobotCell = cell;
              break;
            }
          }
        }

        if (foundOb) {
          const base = "border border-black w-5 h-5 md:w-8 md:h-8 bg-pink-200";
          if (foundOb.d === Direction.WEST) {
            cells.push(<td className={`${base} border-l-4 border-l-red-600`} />);
          } else if (foundOb.d === Direction.EAST) {
            cells.push(<td className={`${base} border-r-4 border-r-red-600`} />);
          } else if (foundOb.d === Direction.NORTH) {
            cells.push(<td className={`${base} border-t-4 border-t-red-600`} />);
          } else if (foundOb.d === Direction.SOUTH) {
            cells.push(<td className={`${base} border-b-4 border-b-red-600`} />);
          } else {
            cells.push(<td className={base} />);
          }
        } else if (foundRobotCell) {
          const isMarker = foundRobotCell.d !== null;
          const isCenter = i === centerT.x && j === centerT.y;

          if (isMarker) {
            // Camera cell stays yellow; red when s != -1
            cells.push(
              <td
                className={classNames(
                  "border w-5 h-5 md:w-8 md:h-8",
                  foundRobotCell.s != -1 ? "bg-red-500" : "bg-yellow-300"
                )}
              />
            );
          } else if (isCenter) {
            // One Mario in center of 3x3
            cells.push(
              <td className="border border-black w-5 h-5 md:w-8 md:h-8 bg-pink-50 relative">
                <img
                  src="/assets/mario.png"
                  alt="Mario"
                  className="pointer-events-none select-none absolute inset-0 m-auto w-6 h-6 md:w-8 md:h-8"
                  style={{ transform: `rotate(${dirToRotationDeg(robotState.d)}deg)` }}
                />
              </td>
            );
          } else {
            // Other robot cells: subtle pink fill to show area
            cells.push(
              <td className="border border-black w-5 h-5 md:w-8 md:h-8 bg-pink-50" />
            );
          }
        } else {
          cells.push(
            <td className="border border-black w-5 h-5 md:w-8 md:h-8 bg-white" />
          );
        }
      }

      rows.push(<tr key={`row-${19 - i}`}>{cells}</tr>);
    }

    const yAxis = [<td key="axis-empty" />];
    for (let i = 0; i < 20; i++) {
      yAxis.push(
        <td key={`axis-${i}`} className="w-5 h-5 md:w-8 md:h-8">
          <span className="text-black font-bold text-[0.6rem] md:text-base ">{i}</span>
        </td>
      );
    }
    rows.push(<tr key="axis-row">{yAxis}</tr>);
    return rows;
  };

  useEffect(() => {
    if (page >= path.length) return;
    setRobotState(path[page]);
  }, [page, path]);

  return (
    <div className="w-full min-h-screen bg-white text-black flex flex-col items-center">
      {/* Header with brand logo */}
      <div className="w-full max-w-6xl flex items-center justify-between py-6 px-4">
        <div className="flex items-center gap-3">
          <img
            src="/assets/mario-kart-logo.svg"
            alt="Logo"
            className="h-40 md:h-48"
            onError={(e) => (e.currentTarget.style.display = "none")}
          />
          <h1 className="text-xl md:text-2xl font-extrabold tracking-tight">Algorithm Simulator</h1>
        </div>
        <span className="inline-block px-3 py-1 rounded-full border border-black bg-pink-100 text-black text-xs md:text-sm">
          Theme: Black · Pink · Red · White
        </span>
      </div>

      <div className="flex flex-row justify-center items-start w-full max-w-6xl space-x-8 px-4 pb-10">
        {/* Left Column: Controls and Obstacles */}
        <div className="flex flex-col space-y-6 w-full max-w-md">
          {/* Robot Position */}
          <div className="flex flex-col items-center text-center bg-white border border-black rounded-2xl shadow p-4">
            <h2 className="text-red-600 font-bold mb-3">Robot Position</h2>
            <div className="form-control">
              <label className="flex items-center gap-2 flex-wrap">
                <span className="px-2 py-1 rounded bg-black text-white">X</span>
                <input
                  onChange={onChangeRobotX}
                  type="number"
                  placeholder="1"
                  min="1"
                  max="18"
                  className="input w-24 text-black placeholder-pink-600 border border-black bg-white"
                />
                <span className="px-2 py-1 rounded bg-black text-white">Y</span>
                <input
                  onChange={onChangeRobotY}
                  type="number"
                  placeholder="1"
                  min="1"
                  max="18"
                  className="input w-24 text-black placeholder-pink-600 border border-black bg-white"
                />
                <span className="px-2 py-1 rounded bg-black text-white">D</span>
                <select
                  onChange={onRobotDirectionInputChange}
                  value={robotDir}
                  className="select w-28 text-black border border-black bg-white"
                >
                  <option value={ObDirection.NORTH}>Up</option>
                  <option value={ObDirection.SOUTH}>Down</option>
                  <option value={ObDirection.WEST}>Left</option>
                  <option value={ObDirection.EAST}>Right</option>
                </select>
                <button
                  className="px-3 py-2 rounded border border-black bg-red-600 text-white hover:bg-red-700"
                  onClick={onClickRobot}
                >
                  Set
                </button>
              </label>
            </div>
          </div>

          {/* Add Obstacles */}
          <div className="flex flex-col items-center text-center bg-white border border-black rounded-2xl shadow p-4">
            <h2 className="text-red-600 font-bold mb-3">Add Obstacles</h2>
            <div className="form-control">
              <label className="flex items-center gap-2 flex-wrap">
                <span className="px-2 py-1 rounded bg-black text-white">X</span>
                <input
                  onChange={onChangeX}
                  type="number"
                  placeholder="1"
                  min="0"
                  max="19"
                  className="input w-24 text-black placeholder-pink-600 border border-black bg-white"
                />
                <span className="px-2 py-1 rounded bg-black text-white">Y</span>
                <input
                  onChange={onChangeY}
                  type="number"
                  placeholder="1"
                  min="0"
                  max="19"
                  className="input w-24 text-black placeholder-pink-600 border border-black bg-white"
                />
                <span className="px-2 py-1 rounded bg-black text-white">D</span>
                <select
                  onChange={onDirectionInputChange}
                  value={directionInput}
                  className="select w-32 text-black border border-black bg-white"
                >
                  <option value={ObDirection.NORTH}>Up</option>
                  <option value={ObDirection.SOUTH}>Down</option>
                  <option value={ObDirection.WEST}>Left</option>
                  <option value={ObDirection.EAST}>Right</option>
                  <option value={ObDirection.SKIP}>None</option>
                </select>
                <button
                  className="px-3 py-2 rounded border border-black bg-pink-600 text-white hover:bg-pink-700"
                  onClick={onClickObstacle}
                >
                  Add
                </button>
              </label>
            </div>
          </div>

          {/* Obstacle Badges */}
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
            {obstacles.map((ob) => (
              <div
                key={ob.id}
                className="flex items-center justify-between gap-2 px-3 py-2 text-black bg-pink-100 rounded-xl border border-black"
              >
                <div className="text-xs md:text-sm">
                  <div>X: {ob.x}</div>
                  <div>Y: {ob.y}</div>
                  <div>D: {DirectionToString[ob.d]}</div>
                </div>
                <button
                  aria-label="Remove obstacle"
                  className="w-6 h-6 flex items-center justify-center rounded-full bg-black text-white"
                  onClick={() => onRemoveObstacle(ob)}
                >
                  ×
                </button>
              </div>
            ))}
          </div>

          {/* Buttons */}
          <div className="flex gap-3 pt-2">
            <button
              className="px-3 py-2 rounded border border-black bg-black text-white hover:opacity-90"
              onClick={onResetAll}
            >
              Reset All
            </button>
            <button
              className="px-3 py-2 rounded border border-black bg-pink-600 text-white hover:bg-pink-700"
              onClick={onReset}
            >
              Reset Robot
            </button>
            <button
              className="px-3 py-2 rounded border border-black bg-red-600 text-white hover:bg-red-700"
              onClick={compute}
            >
              Submit
            </button>
          </div>
        </div>

        {/* Right Column: Grid */}
        <div className="mt-2">
          {path.length > 0 && (
            <div className="flex items-center gap-4 bg-white border border-black px-4 py-3 rounded-xl shadow mb-4">
              <button
                className="w-8 h-8 rounded-full bg-black text-white border border-black hover:opacity-90"
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                title="Previous"
              >
                ‹
              </button>
              <span className="text-black">Step: {page + 1} / {path.length}</span>
              <span className="text-black font-medium">{commands[page]}</span>
              <button
                className="w-8 h-8 rounded-full bg-black text-white border border-black hover:opacity-90"
                disabled={page === path.length - 1}
                onClick={() => setPage(page + 1)}
                title="Next"
              >
                ›
              </button>
            </div>
          )}
          <table className="border-collapse border-none">
            <tbody>{renderGrid()}</tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
