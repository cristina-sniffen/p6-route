import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import javax.management.relation.RelationTypeSupport;

import java.io.File;
import java.io.FileInputStream;

/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 * @author Brandon Fain
 *
 */
public class GraphProcessor {

    private HashMap<Point, ArrayList<Point>> graph = new HashMap<>();
    private Point[] points;

    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */

    public void initialize(FileInputStream file) throws Exception {
        
        /* if the file cannot be read
        if (false)  { 

            throw new Exception("Could not read .graph file");
        } */
        Scanner reader = new Scanner(file);
        int size = reader.nextInt();
        points = new Point[size];
        int i = 0;

        String line = reader.nextLine();
        line = reader.nextLine();

        //put each point in the map with empty arrayList
        while (!reader.hasNextDouble()) {
            String[] pointInfo = line.split(" ");
            Point p = new Point(Double.parseDouble(pointInfo[1]), Double.parseDouble(pointInfo[2]));
            graph.put(p, new ArrayList<>());
            points[i] = p;
            i++;
            line = reader.nextLine();
        }
        
        //add the last point
        String[] pointInfo = line.split(" ");
        Point p = new Point(Double.parseDouble(pointInfo[1]), Double.parseDouble(pointInfo[2]));
        points[i] = p;

        //put the neighbors in based on the edges
        while (reader.hasNextLine()) {
            line = reader.nextLine();
            pointInfo = line.split(" ");
            Point endPoint1 = points[Integer.parseInt(pointInfo[0])];
            Point endPoint2 = points[Integer.parseInt(pointInfo[1])];
            graph.putIfAbsent(endPoint1, new ArrayList<>());
            graph.putIfAbsent(endPoint2, new ArrayList<>());
            graph.get(endPoint1).add(endPoint2);
            graph.get(endPoint2).add(endPoint1);          
        }

        reader.close();
    }


    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * @param p A point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point p) {
        Double shortestDist = Double.POSITIVE_INFINITY;
        Point shortestPoint = p;

        for (Point points : graph.keySet()) {
            Double dist = p.distance(points);
            if (dist < shortestDist) {
                shortestDist = dist;
                shortestPoint = points;
            }
        }

        return shortestPoint;
    }


    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points, 
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * @param start Beginning point. May or may not be in the graph.
     * @param end Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> route) {
        double totalDist = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            totalDist += route.get(i).distance(route.get(i+1));
        }
        return totalDist;
    }
    

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * @param p1 one point
     * @param p2 another point
     * @return true if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point p1, Point p2) {
        Set<Point> visited = new HashSet<>();
        Stack<Point> toExplore = new Stack<>();
        Point current = p1;
        toExplore.push(current);
        visited.add(current);
        while (!toExplore.isEmpty()) {
            current = toExplore.pop();
            for (Point neighbor : graph.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    toExplore.push(neighbor);
                }
                if (visited.contains(p2)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * @param start Beginning point.
     * @param end Destination point.
     * @return The shortest path [start, ..., end].
     * @throws InvalidAlgorithmParameterException if there is no such route, 
     * either because start is not connected to end or because start equals end.
     */
    public List<Point> route(Point start, Point end) throws InvalidAlgorithmParameterException {
        if (!connected(start, end) || start.distance(end) == 0) {
            throw new InvalidAlgorithmParameterException("No path between start and end");
        }
        
        Map<Point, Point> previous = new HashMap<>();
        Map<Point, Double> distance = new HashMap<>();
        ArrayList<Point> ret = new ArrayList<>();

        Comparator<Point> comp = (a, b) -> Double.compare(distance.get(a), distance.get(b));
        PriorityQueue<Point> toExplore = new PriorityQueue<>(comp);

        Point current = start;
        toExplore.add(current);
        distance.put(current, 0.0);

        while (!toExplore.isEmpty()) {
            current = toExplore.remove();
            for (Point neighbor : graph.get(current)) {
                double weight = current.distance(neighbor);
                if (!distance.containsKey(neighbor) || distance.get(neighbor) > distance.get(current) + weight) {
                    distance.put(neighbor, distance.get(current) + weight);
                    previous.put(neighbor, current);
                    toExplore.add(neighbor);
                }
            }
        }

        ret.add(end);
        Point p = previous.get(end);
        ret.add(p);
        for (Point key : previous.keySet()) {
            if (previous.get(p) == null) break;
            p = previous.get(p);
            ret.add(p);
        }

        ArrayList<Point> answer = new ArrayList<>();
        for (int i = ret.size() - 1; i >= 0; i--) {
            answer.add(ret.get(i));
        }

        return answer;
    }
    
    public static void main(String[] args) throws Exception {
        GraphProcessor test = new GraphProcessor();
        File file = new File("data/usa.graph");
        FileInputStream f = new FileInputStream(file);
        test.initialize(f);

        //read first city
        System.out.println("Enter a starting city and state abbreviation: ");
        Scanner reader = new Scanner(System.in);
        String startText = reader.nextLine();
        String[] startDetails = startText.split(" ,");
        Point start = new Point(Double.parseDouble(startDetails[2]), Double.parseDouble(startDetails[3]));
        reader.close();

        //read end city
        System.out.println("Enter a ending city and state abbreviation: ");
        Scanner read = new Scanner(System.in);
        String endText = reader.nextLine();
        String[] endDetails = endText.split(",");
        Point end = new Point(Double.parseDouble(endDetails[2]), Double.parseDouble(endDetails[3]));
        read.close();

        long startTime = System.nanoTime();

        start = test.nearestPoint(start);
        end = test.nearestPoint(end);
        ArrayList<Point> path = (ArrayList<Point>) test.route(start, end);
        double distance = test.routeDistance(path);

        long endTime = System.nanoTime();
        long time = endTime - startTime;
        time = time / 1000000;

        System.out.println("Nearest point to " + startText + ": " + start);
        System.out.println("Nearest point to " + endText + ": " + end);
        System.out.println("Route between these two points: " + distance + " miles");
        System.out.println("Total time to calculate this: " + time + "ms");


        Visualize visualize = new Visualize("data/usa.vis", "images/usa.png");
        visualize.drawRoute(path);
    }
}

