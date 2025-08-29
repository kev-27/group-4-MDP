using System;
// you may add more imports here

namespace ScriptNs
{
    public class ScriptContainer
    {

        // do not modify method declaration i.e. the one line directly below this
        public static string MainScript(int[,] gridLayout, int[] robotPosition, bool posTgridF, bool addObstacle, int[] obstaclePosition)
        {
            
            // Insert your code here

            string stringToSend = "GRID ";

            int height = gridLayout.GetLength(1);
            int width  = gridLayout.GetLength(0);

            stringToSend += height + " ";
            stringToSend += width  + " ";
            stringToSend += robotPosition[0] + 1 + " ";
            stringToSend += robotPosition[1] + 1 + " ";
            stringToSend += robotPosition[2] + " ";

            for (var y = 0; y < height; y++)
            {
                for (var x = 0; x < width; x++)
                {
                    stringToSend += gridLayout[x, y] + " ";
                }
            }

            return stringToSend;

            // End insert code
        }


    }

}