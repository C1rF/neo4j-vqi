package hkust.edu.visualneo;

import hkust.edu.visualneo.utils.backend.DbMetadata;
import hkust.edu.visualneo.utils.frontend.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.neo4j.driver.Value;

import java.io.IOException;
import java.util.*;

public class VisualNeoController {
    /**
     *   7 Buttons in the toolbox and 1 Label indicating the current tool
     */
    @FXML
    private Button btn_clear;
    @FXML
    private Button btn_select;
    @FXML
    private Button btn_vertex;
    @FXML
    private Button btn_edge;
    @FXML
    private Button btn_erase;
    @FXML
    private Button btn_save;
    @FXML
    private Button btn_load;
    @FXML
    private Label label_current_state;
    /**
     *   4 Buttons controlling the core operations
     */
    @FXML
    private Button btn_load_db;
    @FXML
    private Button btn_generate_p;
    @FXML
    private Button btn_exact_search;
    @FXML
    private Button btn_similarity_search;
    /**
     *   Buttons and Labels in the Label and Property Pane (8 in total)
     */
    @FXML
    private ChoiceBox<String> choicebox_node_label;
    @FXML
    private Button btn_add_node_label;
    @FXML
    private ChoiceBox<String> choicebox_relation_label;
    @FXML
    private Button btn_add_relation_label;
    @FXML
    private ChoiceBox<String> choicebox_property_name;
    @FXML
    private ChoiceBox<String> choicebox_property_type;
    @FXML
    private TextField textfield_property_value;
    @FXML
    private Button btn_add_property;
    /**
     *   Buttons and Texts in the Info Pane (4 in total)
     */
    @FXML
    private ScrollPane info_pane;
    @FXML
    private Text text_node_or_relation;
    @FXML
    private Text text_label_info;
    @FXML
    private Text text_property_info;
    /**
     *   Node Label Pane + Relation Label Pane + Property Pane
     */
    @FXML
    private AnchorPane pane_node_label;
    @FXML
    private AnchorPane pane_relation_label;
    @FXML
    private AnchorPane pane_property;
    /**
     *   Drawing Space
     */
    @FXML
    private Pane Drawboard;

    /**
     *   Database Info Pane
     */
    @FXML
    private AnchorPane pane_no_database;
    @FXML
    private AnchorPane pane_with_database;
    @FXML
    private TableView tableview_node;
    @FXML
    private TableColumn<Map, String> node_name_col;
    @FXML
    private TableColumn<Map, String> node_count_col;
    @FXML
    private TableView tableview_relation;
    @FXML
    private TableColumn<Map, String> relation_name_col;
    @FXML
    private TableColumn<Map, String> relation_count_col;


    // The system application
    private VisualNeoApp app;

    // The scene of the main app
    private Scene scene;

    // The current focused element
    private GraphElement current_focused;

    // ALL Status
    public enum Status {EMPTY, VERTEX , EDGE_1, EDGE_2, ERASE, SELECT};
    // Current Status
    public static Status s;

    // A list that stores all the Vertex objects and Edges objects
    public ArrayList<Vertex> listOfVertices = new ArrayList<Vertex>();
    public ArrayList<Edge> listOfEdges = new ArrayList<Edge>();

    // A temperate startVertex
    Vertex startVertex;

    /**
     * The constructor.
     * The constructor is called before initialize() method.
     */
    public VisualNeoController() { }

    public void setApp(VisualNeoApp app) {
        this.app = app;
    }
    public void setScene(Scene scene){
        this.scene = scene;
        scene.focusOwnerProperty().addListener(
                (prop, oldNode, newNode) -> {

                    // Check whether the new node is a Vertex or Edge
                    // Decide whether to show the info pane and label/property panes
                    if(newNode instanceof Circle || newNode instanceof QuadCurve) {
                        // System.out.println("New focus is a GraphElement");
                        // First remove the focus display from the "last" focus
                        if(current_focused != null) current_focused.removeFocused();
                        // Assign the new focus to the current node
                        current_focused = matchGraphElement(newNode);
                        current_focused.becomeFocused();
                        info_pane.setVisible(true);
                        pane_property.setVisible(true);
                        if(newNode instanceof Circle){
                            pane_node_label.setVisible(true);
                            pane_relation_label.setVisible(false);
                            text_node_or_relation.setText("Node Information");
                        }
                        else{
                            pane_node_label.setVisible(false);
                            pane_relation_label.setVisible(true);
                            text_node_or_relation.setText("Relation Information");
                        }

                        // Display the information on the information pane
                        StringBuilder builder = new StringBuilder();
                        text_label_info.setText(current_focused.getLabel());
                        HashMap<String, Value> properties = current_focused.getProp();
                        for (String propertyKey : properties.keySet()) {
                            builder.append(propertyKey).append(" : ").append(properties.get(propertyKey)).append("\n");
                        }
                        text_property_info.setText(builder.toString());
                    }
                    else {
                        // If the focused element is not a Vertex/Edge
                        // System.out.println("New focus is NOT a GraphElement");

                        // If the focused item becomes the DrawBoard or buttons on the toolbox
                        // Remove the current_focused and hide those panes
                        // Otherwise, do nothing

                        Button[] draw_btns = {btn_clear,btn_select,btn_vertex,btn_edge,btn_erase,btn_save,btn_load};
                        boolean is_btn = false;
                        for(int i = 0; i < draw_btns.length; i++){
                            if(draw_btns[i] == newNode) {
                                is_btn = true;
                                break;
                            }
                        }
                        if(newNode == Drawboard || is_btn){
                            if(current_focused != null) current_focused.removeFocused();
                            current_focused = null;
                            info_pane.setVisible(false);
                            pane_node_label.setVisible(false);
                            pane_relation_label.setVisible(false);
                            pane_property.setVisible(false);
                        }
                    }
                });
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        s = Status.EMPTY;
        current_focused = null;
    }

    /**
     * Called when the user clicks on the clear button.
     * Clear the drawing board
     */
    @FXML
    private void handleClear() {
        Drawboard.getChildren().clear();
        listOfVertices.clear();
        listOfEdges.clear();
    }

    /**
     * Called when the user clicks the select button.
     * Select a vertex
     */
    @FXML
    private void handleSelect() {
        s = Status.SELECT;
        label_current_state.setText("Current Tool: Select");
        unlockAllVertices();
        unlockAllEdges();
    }

    /**
     * Called when the user clicks the vertex button.
     * Create a vertex
     */
    @FXML
    private void handleVertex() {
        s = Status.VERTEX;
        label_current_state.setText("Current Tool: Vertex");
        lockAllVertices();
        lockAllEdges();
    }

    /**
     * Called when the user clicks the Edge button.
     * Create an edge
     */
    @FXML
    private void handleEdge() {
        s = Status.EDGE_1;
        label_current_state.setText("Current Tool: Edge");
        unlockAllVertices();
        lockAllEdges();
    }

    /**
     * Called when the user clicks the Erase button.
     * Erase a vertex and all connected edges
     */
    @FXML
    private void handleErase() {
        s = Status.ERASE;
        label_current_state.setText("Current Tool: Erase");
        unlockAllVertices();
        unlockAllEdges();
    }

    /**
     * Called when the user clicks the Save button.
     * Save the drawing pattern
     */
    @FXML
    private void handleSave() {

    }

    /**
     * Called when the user clicks the Load button.
     * Load the drawing pattern
     */
    @FXML
    private void handleLoad() {
        listOfVertices.clear();
        Drawboard.getChildren().clear();
        // Load the new data
    }

    /**
     * Called when the user clicks the drawing board
     */
    @FXML
    private void handleClickOnBoard(MouseEvent m) {
        // System.out.println("Clicked at " + m.getX() + ' ' +  m.getY());
        // If the status is VERTEX, meaning that we need to create the vertex
        if(s == Status.VERTEX) {
            Vertex temp_vertex = new Vertex(Drawboard, m.getX(), m.getY());
            listOfVertices.add(temp_vertex);
            temp_vertex.setScene(scene);
            temp_vertex.requestFocus();
        }

        // If the status is ERASE, meaning that we need to erase the focused Vertex/Edge
        if(s == Status.ERASE) {

            // Check whether it is a Vertex
            if(current_focused instanceof Vertex) {
                Vertex focused_vertex = (Vertex) current_focused;
                // First remove all its connected edges
                int numOfEdges = listOfEdges.size();
                List<Integer> index_list = new ArrayList<Integer>();
                for(int j=0; j<numOfEdges; j++){
                    Edge temp_edge = listOfEdges.get(j);
                    if( temp_edge.startVertex == focused_vertex || temp_edge.endVertex == focused_vertex){
                        // Remove the edge both from the board and the ArrayList
                        temp_edge.eraseShapes();
                        index_list.add(j);
                    }
                }
                for(int t = index_list.size()-1; t >= 0; t--){
                    int j = index_list.get(t);
                    listOfEdges.remove(j);
                }
                // Then remove the vertex itself
                focused_vertex.eraseShapes();
                listOfVertices.remove(focused_vertex);
                // For testing
                System.out.println("Successfully removed a vertex");
            }

            // Check whether it is an Edge
            if(current_focused instanceof Edge) {
                Edge focused_edge = (Edge) current_focused;
                Vertex this_startVertex = focused_edge.startVertex;
                Vertex this_endVertex = focused_edge.endVertex;
                focused_edge.eraseShapes();
                listOfEdges.remove(focused_edge);
                updateEdgeShape(this_startVertex, this_endVertex);
                System.out.println("Successfully removed an edge");
            }
        }

        // If the status is EDGE_1/EDGE_2, meaning that we are forming the EDGE
        if(s == Status.EDGE_1 || s == Status.EDGE_2) {

            if(!(current_focused instanceof Vertex)) return;

            Vertex focused_vertex = (Vertex) current_focused;
            // If the status is EDGE_2, meaning that we are choosing the second Vertex
            if(s == Status.EDGE_2){
                // Create a new Edge between the two if they are not the same vertex
                //if(startVertex != focused_vertex){
                    Edge temp_edge =  new Edge(Drawboard, startVertex, focused_vertex, false);
                    listOfEdges.add(temp_edge);
                    updateEdgeShape(startVertex, focused_vertex);
                    startVertex = null;
                    temp_edge.setScene(scene);
                    temp_edge.requestFocus();
                //}
//                else{
//                    Drawboard.requestFocus();
//                }
                // No matter whether the edge is created or not
                // Return to EDGE_1 state
                s = Status.EDGE_1;
            }
            // If the status is EDGE_1, meaning that we are choosing the first Vertex
            else if(s == Status.EDGE_1) {
                startVertex = focused_vertex;
                s = Status.EDGE_2;
            }
        }
        // If the current state is SELECT, we move the focus to the drawing board.
        // Note that here we already exclude the case when user clicks a vertex or an edge
        // Because in that case, the mouse event will be consumed in the Vertex/Edge object
        // and will not be passed to drawing board
        if(s == Status.SELECT) {
            Drawboard.requestFocus();
        }
    }

    /**
     * Called when the user drag on the drawing board
     */
    @FXML
    private void handleDragOnBoard() {
        if (s != Status.SELECT || !(current_focused instanceof Vertex)) return;
        // If the status is SELECT and current_focused is a Vertex
        // we need to move all edges that connect to it
        Vertex focused_vertex = (Vertex) current_focused;
        int numOfEdges = listOfEdges.size();
        for(int j=0; j<numOfEdges; j++){
            Edge temp_edge = listOfEdges.get(j);
            if( temp_edge.startVertex == focused_vertex || temp_edge.endVertex == focused_vertex){
                temp_edge.setCurve();
            }
        }
        // System.out.println("Move all connected edges");
    }

    /**
     * Called when the user click on Load Database button
     */
    @FXML
    private void handleLoadDB() throws IOException {
        // Pop up the load database window
        FXMLLoader fxmlLoader = new FXMLLoader(VisualNeoController.class.getResource("fxml/load-database.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 292, 280);
        LoadDatabaseController db_controller = fxmlLoader.<LoadDatabaseController>getController();
        db_controller.setVisualNeoController(this);
        Stage stage = new Stage();
        stage.setTitle("Load Database");
        stage.setScene(scene);
        stage.show();
    }

    public void submitDBInfo(String uri, String user, String password){
        //System.out.println(uri +"  "+ user +"  "+ password);
        app.queryHandler.loadDatabase(uri, user, password);
    }

    public void updateUIWithMetaInfo(){
        DbMetadata metadata = app.queryHandler.getMeta();
        // TODO: Update the DatabaseInfo Pane first
        // First switch the display pane
        pane_with_database.setVisible(true);
        pane_no_database.setVisible(false);
        // Update node table
        UpdateNodeTable(metadata);
        UpdateRelationTable(metadata);
        // TODO: Show the schema

        // Then update the choiceboxs with correct choices
        metadata.nodeLabels().forEach(label -> choicebox_node_label.getItems().add(label));
        metadata.relationLabels().forEach(label -> choicebox_relation_label.getItems().add(label));
        metadata.propertyKeys().forEach(property -> choicebox_property_name.getItems().add(property));
        btn_add_node_label.setDisable(false);
        btn_add_relation_label.setDisable(false);
        btn_add_property.setDisable(false);
        choicebox_node_label.getSelectionModel().selectFirst();
        choicebox_relation_label.getSelectionModel().selectFirst();
        choicebox_property_name.getSelectionModel().selectFirst();
    }

    /**
     * Called when the user click on Generate Patterns button
     */
    @FXML
    private void handleGeneratePatterns() {
    }

    /**
     * Called when the user click on Exact Search button
     */
    @FXML
    private void handleExactSearch() {
        app.queryHandler.exactSearch(listOfVertices, listOfEdges);
    }

    /**
     * Called when the user click on Similarity Search button
     */
    @FXML
    private void handleSimilaritySearch() {
    }

    @FXML
    void handleAddLabel() {
        // TODO: Split this to two functions
        // Add Node labels to the Vertex/Edge
        if(current_focused != null){
            try{
                if(current_focused instanceof Vertex)
                    current_focused.addLabel(choicebox_node_label.getValue());
                else
                    current_focused.addLabel(choicebox_relation_label.getValue());
                System.out.println("Successfully add the label");
            }catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Label Addition Error");
                alert.setHeaderText("Cannot add the label.");
                alert.setContentText("Please select the correct element!");
                alert.showAndWait();
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Label Addition Error");
            alert.setHeaderText("Cannot add the label.");
            alert.setContentText("Please select the correct element!");
            alert.showAndWait();
        }
    }

    @FXML
    void handleAddProperty() {
    }

    /**
     * Called when the user's mouse enter a button
     */
    @FXML
    private void handleMouseEnterButton(MouseEvent m) {
        Drawboard.getScene().setCursor(Cursor.HAND);
    }
    /**
     * Called when the user's mouse leaves a button
     */
    @FXML
    private void handleMouseLeaveButton(MouseEvent m) {
        Drawboard.getScene().setCursor(Cursor.DEFAULT);
    }

    private void lockAllVertices(){
        int numOfVertices = listOfVertices.size();
        for(int i =0; i < numOfVertices; i++) {
            listOfVertices.get(i).canSelect = false;
        }
    }
    private void lockAllEdges(){
        int numOfEdges = listOfEdges.size();
            for(int i =0; i < numOfEdges; i++) {
            listOfEdges.get(i).canSelect = false;
        }
    }
    private void unlockAllVertices(){
        int numOfVertices = listOfVertices.size();
        for(int i =0; i < numOfVertices; i++) {
            listOfVertices.get(i).canSelect = true;
        }
    }
    private void unlockAllEdges(){
        int numOfEdges = listOfEdges.size();
        for(int i =0; i < numOfEdges; i++) {
            listOfEdges.get(i).canSelect = true;
        }
    }
    public static Status getStatus(){
        return s;
    }

    private GraphElement matchGraphElement(javafx.scene.Node graphNode){
        if(graphNode instanceof Circle){
            // Find a Vertex that contains the circle
            for(int i=0; i<listOfVertices.size(); i++){
                if(listOfVertices.get(i).containCircle((Circle) graphNode))
                    return listOfVertices.get(i);
            }
        }
        else{
            // Find an Edge that contains the edge
            for(int i=0; i<listOfEdges.size(); i++){
                if(listOfEdges.get(i).containEdge((QuadCurve) graphNode))
                    return listOfEdges.get(i);
            }
        }
        return null;
    }

    private void UpdateNodeTable(DbMetadata metadata){
        node_name_col.setCellValueFactory(new MapValueFactory<>("Label"));
        node_count_col.setCellValueFactory(new MapValueFactory<>("Count"));
        ObservableList<Map<String, Object>> items =
                FXCollections.<Map<String, Object>>observableArrayList();
        Set<String> nodeLabels = metadata.nodeLabels();
        for (String label : nodeLabels) {
            Map<String, Object> temp_item = new HashMap<>();
            temp_item.put("Label", label);
            temp_item.put("Count" , metadata.nodeCountOf(label) );
            items.add(temp_item);
        }
        Map<String, Object> final_item = new HashMap<>();
        final_item.put("Label", "#SUM");
        final_item.put("Count" , metadata.nodeCount() );
        items.add(final_item);
        tableview_node.getItems().addAll(items);
    }
    private void UpdateRelationTable(DbMetadata metadata){
        relation_name_col.setCellValueFactory(new MapValueFactory<>("Label"));
        relation_count_col.setCellValueFactory(new MapValueFactory<>("Count"));
        ObservableList<Map<String, Object>> items =
                FXCollections.<Map<String, Object>>observableArrayList();
        Set<String> relationLabels = metadata.relationLabels();
        for (String label : relationLabels) {
            Map<String, Object> temp_item = new HashMap<>();
            temp_item.put("Label", label);
            temp_item.put("Count" , metadata.relationCountOf(label) );
            items.add(temp_item);
        }
        Map<String, Object> final_item = new HashMap<>();
        final_item.put("Label", "#SUM");
        final_item.put("Count" , metadata.relationCount() );
        items.add(final_item);
        tableview_relation.getItems().addAll(items);
    }

    private void updateEdgeShape(Vertex startVertex, Vertex endVertex){
        int count = 0;
        ArrayList<Edge> temp_list = new ArrayList<>();
        for(int i=0; i<listOfEdges.size(); i++){
            Edge temp = listOfEdges.get(i);
            if((temp.startVertex == startVertex && temp.endVertex==endVertex) ||
                    (temp.endVertex == startVertex && temp.startVertex==endVertex)
            ){
                count++;
                temp_list.add(temp);
            }
        }
        for(int i=0; i<count; i++){
            Edge temp_to_update = temp_list.get(i);
            temp_to_update.updateOffset(count, i);
            temp_to_update.setCurve();
        }

    }

}
