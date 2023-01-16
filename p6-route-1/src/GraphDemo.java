import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Demonstrates the calculation of shortest paths in the US Highway
 * network, showing the functionality of GraphProcessor and using
 * Visualize
 * To do: Add your name(s) as authors
 */
public class GraphDemo {
    public static void main(String[] args) throws Exception {
        GraphProcessor test = new GraphProcessor();
        File file = new File("data/usa.graph");
        FileInputStream f = new FileInputStream(file);
        test.initialize(f);

        //read first city
        System.out.println("Enter starting coordinates: ");
        Scanner reader = new Scanner(System.in);
        String[] startCoordinates = reader.nextLine().split(",");
        Point start = new Point(Double.parseDouble(startCoordinates[0]), Double.parseDouble(startCoordinates[1]));

        //read end city
        System.out.println("Enter ending coordinates: ");
        Scanner read = new Scanner(System.in);
        String[] endCoordinates = read.nextLine().split(",");
        Point end = new Point(Double.parseDouble(endCoordinates[0]), Double.parseDouble(endCoordinates[1]));
        read.close();
        reader.close();

        long startTime = System.nanoTime();

        start = test.nearestPoint(start);
        end = test.nearestPoint(end);
        ArrayList<Point> path = (ArrayList<Point>) test.route(start, end);
        double distance = test.routeDistance(path);

        long endTime = System.nanoTime();
        long time = endTime - startTime;
        time = time / 1000000;

        System.out.println("Nearest point to (" + startCoordinates[0] + "," + startCoordinates[1] + "): " + start);
        System.out.println("Nearest point to (" + endCoordinates[0] + "," + endCoordinates[1] + "): " + end);
        System.out.println("Route between these two points: " + distance + " miles");
        System.out.println("Total time to calculate this: " + time + "ms");


        Visualize visualize = new Visualize("data/usa.vis", "images/usa.png");
        visualize.drawRoute(path);
    }
}