import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WavyTreePrinter {
    private static class Graph {
        private final List<Node> roots = new ArrayList<>();

        public void DFSTraverse() {
            for (Node root : roots) {
                DFSUtil(root, 0);
            }
        }

        private void DFSUtil(Node node, int depth) {
            for (int i = 0; i < depth; i++) {
                System.out.print("----");
            }
            System.out.println(node.name);
            for (Node child : node.children) {
                DFSUtil(child, depth + 1);
            }
        }

        // constructs an arraylist of roots for our forest.
        Graph(Map<String, List<Integer>> processToParentPid) {
            //map for storing our nodes
            Map<Integer, Node> nodes = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : processToParentPid.entrySet()) {
                String name = entry.getKey();
                int pid = entry.getValue().get(0);
                int parentPid = entry.getValue().get(1);

                nodes.put(pid, new Node(name, pid, parentPid));
            }

            // connect the nodes and fill the roots list
            for (Node node : nodes.values()) {
                if (node.parentPid == 0) {
                    roots.add(node);
                } else {
                    Node parent = nodes.get(node.parentPid);
                    if (parent != null) {
                        parent.children.add(node);
                    }
                }
            }
            // create a sort for organizing the tree roots in ascending order in the list.
            roots.sort(Comparator.comparing(node -> node.name));
        }

        private static class Node {
            private final String name;
            private final int pid;
            private final int parentPid;
            private final List<Node> children = new ArrayList<>();
            Node(String name, int pid, int parentPid) {
                this.name = name;
                this.pid = pid;
                this.parentPid = parentPid;
            }
        }
    }

    // used for Gson TypeToken when extracting data from json
    private static class Process {
        private String process_name;
        private int pid;
        private int parent_pid;
    }

    public static class ParserStringToJSON {
        public static Map<String, List<Integer>> parse(String s) {
            //type erasure erases the process type during runtime, hence Gson has the TypeToken
            //to help with object extraction from string.
            Type listType = new TypeToken<List<Process>>() {
            }.getType();
            List<Process> processes = new Gson().fromJson(s, listType);
            Map<String, List<Integer>> processToParentPid = new HashMap<>();

            for (Process process : processes) {
                processToParentPid.put(process.process_name, Arrays.asList(process.pid, process.parent_pid));
            }
            return processToParentPid;
        }

        public static Map<String, List<Integer>> parse(File file) throws IOException {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return parse(content);
        }
    }

    public static void main(String[] args) {
        String jsonString = "[\n" +
                "    {\"process_name\":\"a.exe\", \"pid\":420, \"parent_pid\":428},\n" +
                "    {\"process_name\":\"c.exe\", \"pid\":428, \"parent_pid\":null},\n" +
                "    {\"process_name\":\"d.exe\", \"pid\":551, \"parent_pid\":420},\n" +
                "    {\"process_name\":\"e.exe\", \"pid\":552, \"parent_pid\":428},\n" +
                "    {\"process_name\":\"f.exe\", \"pid\":553, \"parent_pid\":null},\n" +
                "    {\"process_name\":\"g.exe\", \"pid\":4, \"parent_pid\":553},\n" +
                "    {\"process_name\":\"b.exe\", \"pid\":7, \"parent_pid\":4},\n" +
                "    {\"process_name\":\"h.exe\", \"pid\":11, \"parent_pid\":7}\n" +
                "]";

        // path to the file must be provided
        String filePath = "/home/kostyavar/playgroundProjects/noName/src/pnames.json";
        File file = new File(filePath);
        try {
            // print from file
            Map<String, List<Integer>> processToParentPidFromFile = ParserStringToJSON.parse(file);
            Graph dfs2 = new Graph(processToParentPidFromFile);
            dfs2.DFSTraverse();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }

        // print from string
        Map<String, List<Integer>> processToParentPidFromString = ParserStringToJSON.parse(jsonString);
        Graph dfs = new Graph(processToParentPidFromString);
        dfs.DFSTraverse();
    }
}