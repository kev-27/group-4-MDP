using System;
namespace ScriptNs
{
    public class ScriptContainer
    {


        public static string MainScript(int[,] gridLayout, int[] robotPosition, bool posTgridF, bool addObstacle, int[] obstaclePosition)
        {
            string stringToSend = "";

            if (posTgridF)
            {
                stringToSend = "ROBOTPOSITION:" + robotPosition[0] + ", " + robotPosition[1] +", " + robotPosition[2];
            }
            else
            {
                if (addObstacle)
                {
                    stringToSend = "ADDOBSTACLE: " + obstaclePosition[0] + "," + obstaclePosition[1];
                }
                else
                {
                    stringToSend = "REMOVEOBSTACLE: " + obstaclePosition[0] + "," + obstaclePosition[1];
                }
            }

            return stringToSend;
        }


    }

}