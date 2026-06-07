package com.example.parking.Service;

import com.example.parking.Entities.GraphNode;
import com.example.parking.Entities.Parking;
import com.example.parking.Reposetories.ParkingRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ParkingGraphService {

    @Autowired
    private ParkingRepo parkingRepository;

    private final Map<GraphNode, List<GraphNode>> adjacencyList = new ConcurrentHashMap<>();
    private final Map<String, GraphNode> allNodesIndex = new HashMap<>();
    private final Map<String, GraphNode> laneNodes = new HashMap<>();

    private GraphNode findOrRegisterLane(long floor, String laneName, int index) {
        String laneId = "F" + floor + "-Lane_" + laneName + "_" + index;
        return laneNodes.computeIfAbsent(laneId, id -> {
            GraphNode newNode = new GraphNode(id, true);
            allNodesIndex.put(id, newNode);
            return newNode;
        });
    }


    @PostConstruct
    // 🌟 נשנה את השם או נשאיר, העיקר שנבטיח שהנתונים קיימים לפני ה-findAll!
    public void initGraphFromDb() {

        // 🛠️ שלב 0: הגנה! אם ה-DB ריק, נייצר את החניות כאן ועכשיו לפני ששולפים
        if (parkingRepository.count() == 0) {
            System.out.println("⚠️ בסיס הנתונים ריק! מייצר 150 חניות ראשוניות...");
            for (long f = 1; f <= 3; f++) {
                for (char row = 'A'; row <= 'E'; row++) {
                    for (int spot = 1; spot <= 10; spot++) {
                        Parking p = new Parking();
                        p.setFloor(f);
                        p.setRow(String.valueOf(row));
                        p.setIndex((long) spot);
                        p.setLocation(f + "-" + row + "-" + spot);
                        p.setPricePerHour(10.0 + f);
                        p.setSpotType("REGULAR");
                        p.setOccupied(false);
                        if (f == 1 && row == 'A' && spot <= 6) p.setOccupied(true);

                        parkingRepository.save(p);
                    }
                }
            }
            System.out.println("✅ 150 חניות נוצרו בהצלחה ב-DB.");
        }

        // עכשיו בטוח יש חניות ב-DB! נשלוף אותן
        List<Parking> allSpots = parkingRepository.findAll();
        System.out.println(">>> מספר חניות שנטענו מתוך ה-DB לגרף: " + allSpots.size());

        try {
            // 1. יצירת כל צמתי החניות
            for (Parking spot : allSpots) {
                // 🌟 שים לב: שיניתי לפורמט התואם למה שהפרונט-אנד מחפש (1-C-5 ולא F1-C-5)
                // לפי ה-getLocation שלך: f + "-" + row + "-" + spot
                String spotId = spot.getFloor() + "-" + spot.getRow() + "-" + spot.getIndex();
                GraphNode spotNode = new GraphNode(spotId, false);
                allNodesIndex.put(spotId, spotNode);
            }
            System.out.println(">>> שלב 1 הושלם - חניות נוצרו בגרף");

            List<Character> rows = List.of('A', 'B', 'C', 'D', 'E');
            List<String> laneNames = new ArrayList<>();
            laneNames.add("left_A");
            for (int i = 0; i < rows.size() - 1; i++) {
                laneNames.add(rows.get(i) + "-" + rows.get(i + 1));
            }
            laneNames.add("right_E");

            for (long floor = 1; floor <= 3; floor++) {

                // 2. יצירת 10 חוליות לכל נתיב וחיבורן בשרשרת אנכית
                for (String laneName : laneNames) {
                    GraphNode prev = null;
                    for (int idx = 1; idx <= 10; idx++) {
                        GraphNode laneNode = findOrRegisterLane(floor, laneName, idx);
                        if (prev != null) addConnection(prev, laneNode);
                        prev = laneNode;
                    }
                }
                System.out.println(">>> שלב 2 הושלם - נתיבים נוצרו לקומה " + floor);

                // 3. חיבור כל חניה לחוליה המתאימה בנתיב שמשמאלה ומימינה
                for (int i = 0; i < rows.size(); i++) {
                    char row = rows.get(i);
                    String leftLaneName  = (i == 0) ? "left_A" : rows.get(i-1) + "-" + row;
                    String rightLaneName = (i == rows.size()-1) ? "right_E" : row + "-" + rows.get(i+1);

                    for (int spot = 1; spot <= 10; spot++) {
                        String spotId = floor + "-" + row + "-" + spot;
                        GraphNode spotNode = allNodesIndex.get(spotId);
                        if (spotNode == null) continue;

                        // 🌟 טיפ בטיחות: אם laneNodes לא מכיל את הנתיב, נסה לבדוק אם הוא ב-allNodesIndex
                        String leftLaneKey = "F" + floor + "-Lane_" + leftLaneName  + "_" + spot;
                        String rightLaneKey = "F" + floor + "-Lane_" + rightLaneName + "_" + spot;

                        GraphNode leftLane  = laneNodes.containsKey(leftLaneKey) ? laneNodes.get(leftLaneKey) : allNodesIndex.get(leftLaneKey);
                        GraphNode rightLane = laneNodes.containsKey(rightLaneKey) ? laneNodes.get(rightLaneKey) : allNodesIndex.get(rightLaneKey);

                        if (leftLane  != null) addConnection(spotNode, leftLane);
                        if (rightLane != null) addConnection(spotNode, rightLane);
                    }
                }
                System.out.println(">>> שלב 3 הושלם - חניות חוברו לקומה " + floor);

                // 4. כביש עליון ותחתון
                GraphNode prevTop    = null;
                GraphNode prevBottom = null;

                for (String laneName : laneNames) {
                    String topId = "F" + floor + "-CrossTop_" + laneName;
                    GraphNode topNode = new GraphNode(topId, true);
                    allNodesIndex.put(topId, topNode);

                    String botId = "F" + floor + "-CrossBottom_" + laneName;
                    GraphNode botNode = new GraphNode(botId, true);
                    allNodesIndex.put(botId, botNode);

                    // 🌟 שוב, הגנה כפולה לשליפת הנתיבים העליונים והתחתונים
                    String laneTopKey = "F" + floor + "-Lane_" + laneName + "_1";
                    String laneBottomKey = "F" + floor + "-Lane_" + laneName + "_10";

                    GraphNode laneTop = laneNodes.containsKey(laneTopKey) ? laneNodes.get(laneTopKey) : allNodesIndex.get(laneTopKey);
                    GraphNode laneBottom = laneNodes.containsKey(laneBottomKey) ? laneNodes.get(laneBottomKey) : allNodesIndex.get(laneBottomKey);

                    if (laneTop != null) addConnection(topNode, laneTop);
                    if (laneBottom != null) addConnection(botNode, laneBottom);

                    if (prevTop != null) addConnection(prevTop, topNode);
                    if (prevBottom != null) addConnection(prevBottom, botNode);

                    prevTop = topNode;
                    prevBottom = botNode;
                }
                System.out.println(">>> שלב 4 הושלם - כביש עליון/תחתון נוצרו לקומה " + floor);
            }

            // 5. שתי כניסות בקומה 1
            GraphNode entrance1 = new GraphNode("Entrance-1", true);
            GraphNode entrance2 = new GraphNode("Entrance-2", true);
            allNodesIndex.put("Entrance-1", entrance1);
            allNodesIndex.put("Entrance-2", entrance2);

            GraphNode topLeft  = allNodesIndex.get("F1-CrossTop_left_A");
            GraphNode topRight = allNodesIndex.get("F1-CrossTop_right_E");
            if (topLeft  != null) addConnection(entrance1, topLeft);
            if (topRight != null) addConnection(entrance2, topRight);
            System.out.println(">>> שלב 5 הושלם - כניסות נוצרו");

            // 6. חיבור קומות עם מעליות
            connectFloorsWithTwoElevators();

            // הדפסת הדו"ח המשמח!
            printGraphSummary();

        } catch (Exception e) {
            System.err.println("!!! GRAPH INIT FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectFloorsWithTwoElevators() {
        List<Long> floors = List.of(1L, 2L, 3L);

        GraphNode elevator1 = new GraphNode("Elevator-1", true);
        GraphNode elevator2 = new GraphNode("Elevator-2", true);
        allNodesIndex.put(elevator1.getId(), elevator1);
        allNodesIndex.put(elevator2.getId(), elevator2);

        for (Long floor : floors) {
            // מעלית 1 — מחוברת רק ל-CrossBottom_left_A
            GraphNode bottomLeft  = allNodesIndex.get("F" + floor + "-CrossBottom_left_A");
            // מעלית 2 — מחוברת רק ל-CrossBottom_right_E
            GraphNode bottomRight = allNodesIndex.get("F" + floor + "-CrossBottom_right_E");

            if (bottomLeft  != null) addConnection(bottomLeft,  elevator1);
            if (bottomRight != null) addConnection(bottomRight, elevator2);
        }
    }


    private void addConnection(GraphNode node1, GraphNode node2) {
        adjacencyList.computeIfAbsent(node1, k -> new ArrayList<>()).add(node2);
        adjacencyList.computeIfAbsent(node2, k -> new ArrayList<>()).add(node1);
    }

    // אלגוריתם ה-BFS למציאת מסלול (נשאר זהה ומתבסס על המבנה החדש)
    public List<String> calculatePath(String startId, String targetId) {
        System.out.println("=9999999999999999=================");

        if (!allNodesIndex.containsKey(startId)) {
            System.out.println("=999=================");

            throw new ResponseStatusException(

                    HttpStatus.NOT_FOUND, "Start node not found: " + startId
            );
        }
        if (!allNodesIndex.containsKey(targetId)) {
            System.out.println("=99=================");

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Target node not found: " + targetId
            );
        }
        System.out.println("=====================================");

        GraphNode startNode = findNodeById(startId);
        GraphNode targetNode = findNodeById(targetId);

        System.out.println("===111111111=============================");

        Queue<GraphNode> queue = new LinkedList<>();
        Map<GraphNode, GraphNode> parentMap = new HashMap<>();
        Set<GraphNode> visited = new HashSet<>();

        System.out.println("==22222222222==============================");

        queue.add(startNode);
        visited.add(startNode);

        System.out.println("==3333=================================");

        while (!queue.isEmpty()) {
            GraphNode current = queue.poll();

            if (current.equals(targetNode)) {
                return reconstructPath(parentMap, targetNode);
            }

            for (GraphNode neighbor : adjacencyList.getOrDefault(current, new ArrayList<>())) {
                boolean isAccessible = neighbor.isLane() || neighbor.equals(targetNode);
                boolean isOccupied = !neighbor.isLane() && isOccupied(neighbor.getId());

                System.out.println("4===================================");

                if (!visited.contains(neighbor) && isAccessible && (!isOccupied || neighbor.equals(targetNode))) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
            System.out.println("=====================================");

        }
        return Collections.emptyList();
    }

    private boolean isOccupied(String nodeId) {
        try {
            String[] parts = nodeId.split("-");
            if (parts.length < 3) return false;

            int floor = Integer.parseInt(parts[0]);
            String row = parts[1];
            // במקרה של נתיב משני עם סיומת "_2", ננקה את הסטרינג לצורך בדיקה ב-DB
            if(row.contains("_")) row = row.split("_")[0];
            int index = Integer.parseInt(parts[2]);

            return parkingRepository.existsByFloorAndRowAndIndexAndIsOccupied(floor, row, index, true);
        } catch (Exception e) {
            return false;
        }
    }

    private GraphNode findNodeById(String id) {
        GraphNode node = allNodesIndex.get(id);
        if (node == null) {
            throw new IllegalArgumentException("Node not found: " + id);
        }
        return node;
    }

    private List<String> reconstructPath(Map<GraphNode, GraphNode> parentMap, GraphNode target) {
        List<String> path = new ArrayList<>();
        GraphNode current = target;
        while (current != null) {
            path.add(current.getId());
            current = parentMap.get(current);
        }
        Collections.reverse(path);
        return path;
    }
    public void printGraphSummary() {
        System.out.println("====== דו\"ח מבנה הגרף של החניון ======");

        // 1. ספירת חניות
        long spotsCount = allNodesIndex.values().stream().filter(n -> !n.isLane() && !n.getId().contains("Elevator")).count();
        // 2. ספירת נתיבים (ראשיים ומשניים)
        long lanesCount = laneNodes.size();
        // 3. ספירת מעליות
        long elevatorsCount = allNodesIndex.values().stream().filter(n -> n.getId().contains("Elevator")).count();

        System.out.println("🚗 כמות חוליות חניה: " + spotsCount);
        System.out.println("🛣️ כמות חוליות נתיב: " + lanesCount);
        System.out.println("🛗 כמות חוליות מעלית: " + elevatorsCount);
        System.out.println("📊 סך כל החוליות בגרף (הצמתים): " + allNodesIndex.size());
        System.out.println("=====================================");
        System.out.println("====== רשימת כל החוליות בגרף (ממוין) ======");

        if (allNodesIndex.isEmpty()) {
            System.out.println("הגרף ריק. לא נוצרו חוליות.");
        } else {
            allNodesIndex.keySet().stream()
                    .sorted() // מיון אלפביתי לנוחות הקריאה
                    .forEach(nodeId -> System.out.println(" - " + nodeId));
        }

        System.out.println("==========================================");
    }
    // הוסף את זה בתוך ParkingGraphService.java
    public Map<GraphNode, List<GraphNode>> getAdjacencyList() {
        return this.adjacencyList;
    }
}