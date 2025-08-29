using System;
// you may add more imports here

namespace ScriptNs
{
    public class ScriptContainer
    {

        // do not modify method declaration i.e. the one line directly below this
        public static string MainScript(int[,] gridLayout, int[] robotPosition, bool posTgridF, bool addObstacle, int[] obstaclePosition)
        {
            /* 
            gridLayout is the 2D array of the arena layout. 
            To manipulate it, use gridLayout[x,y], where x and y are the x-position and y-position of the arena with origin at the top left, 
            x increases towards the right, y increases towards bottom.            
             _ _ _
            |_|_|_|
            |_|_|A|
            |_|_|_|

            E.g Given the arena above, position A will be gridLayout[2,1]

            if gridLayout[x,y] == 0, it means there is no obstacle at the position
            if gridLayout[x,y] == 1, obstacle exist at that position
            */

            /*
            robotPosition[0] is the x-position of the robot in the arena.
            robotPosition[1] is the y-position of the robot in the arena.
            robotPosition[2] is the direction the robot is facing.

            The x and y positions starts from the top left and increases towards the bottom right, similar with the gridLayout. (See above for gridLayout notes)
            The direction is the angle facing north at 0, and increases clockwise.
            */

            /*
            posTgridF will be true when there is a change in position or direction of the robot on the software
            posTgridF will be false when you add or remove an obstacle on the software
            posTgridF will be null when you send the command to the software to send arena info.

            You may add your code for to format the gridLayout and robotPosition to the respective if-else sections

            You may remove the if-else statement if you want to always send your information, 
            e.g. always send both arena grid information and robot position into a single format, which could be done in JSON format
            */

            /*
            addObstacle will be true  when an obstacle is added on the software
            addObstacle will be false when an obstacle is removed on the software            

            this is typically used with obstaclePosition,
            which is the x,y position of the obstacle that you click to add or remove the obstacle on the software.
            obstaclePosition[x,y]

            You can use this accordingly on how you want to update the map on your Android remote app.
            You can refer to the existing scripts for examples.
            */


            // Insert your code here

            string stringToSend = "";

            if (posTgridF)
            {
                // add code here to format the robot position
            }
            else if (!posTgridF)
            {
                // add code here to format the arena grid information
            }
            else
            {
                // if intend to separately handle what you receive when you send command to software for arena info
            }

            return stringToSend;

            // End insert code
        }


    }

}