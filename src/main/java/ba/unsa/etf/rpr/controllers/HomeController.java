package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.business.MealManager;
import ba.unsa.etf.rpr.business.UserManager;
import ba.unsa.etf.rpr.domain.Meal;
import ba.unsa.etf.rpr.domain.TypeOfMeal;
import ba.unsa.etf.rpr.domain.User;
import ba.unsa.etf.rpr.exceptions.OrderException;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class HomeController extends AbstractController {
    private final UserManager userManager= new UserManager();
    private final MealManager mealManager = new MealManager();
    public Label usernameLabel;
    public Label nameLabel;
    public Label surnameLabel;
    public ListView cartList;
    private String username;
    private List<Meal> selectedMeals;
    private User user;
    @FXML
    private TableView<Meal> mealsTable;
    @FXML
    private TableColumn<Meal,String> nameColumn;
    @FXML
    private TableColumn<Meal, Double> priceColumn;
    @FXML
    private TableColumn<Meal,Integer> quantityColumn;
    @FXML
    private TableColumn<Meal, String> typeColumn;

    public HomeController(String username) {
        this.username = username;
        selectedMeals = new LinkedList<>();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        refreshMeals();

        usernameLabel.setText(username);
        try {
            user = userManager.getByUsername(username);
            nameLabel.setText(user.getName());
            surnameLabel.setText(user.getSurname());
        } catch (OrderException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Adds a selected item (meal) from tableview to list of meals and meals name, quantity and price to listview (cart)
     * @param actionEvent
     */
    public void addToCartClick(ActionEvent actionEvent){
        Meal meal = mealsTable.getSelectionModel().getSelectedItem();
        if(meal == null) {
            new Alert(Alert.AlertType.WARNING,"Please select a meal",ButtonType.OK).showAndWait();
            return;
        }
        selectedMeals.add(meal);
        StringBuilder builder = new StringBuilder();
        builder.append(meal.getName()).append(" ").append(meal.getQuantity()).append("gr ").append(meal.getPrice()).append(" KM");
        cartList.getItems().add(builder);
    }

    /**
     * Removes an item from listview (cart) and corresponding meal from list of meals
     * @param actionEvent
     */
    public void removeFromCartClick(ActionEvent actionEvent) {
        int selectedIndex = cartList.getSelectionModel().getSelectedIndex();
        if(selectedIndex==-1) {
            new Alert(Alert.AlertType.WARNING,"Please select a meal",ButtonType.OK).showAndWait();
            return;
        }
        selectedMeals.remove(selectedIndex);
        cartList.getItems().remove(selectedIndex);
    }
    /**
     * Fetches meals from database
     */
    private void refreshMeals(){
        try{
            mealsTable.setItems(FXCollections.observableList(mealManager.getAll()));
            mealsTable.refresh();
        }
        catch(OrderException e){
            new Alert(Alert.AlertType.ERROR,e.getMessage(), ButtonType.CLOSE).showAndWait();
        }
    }

    /**
     * Opens a new window - About app section
     * @param actionEvent
     * @throws Exception
     */
    public void clickAbout(ActionEvent actionEvent) throws Exception{
        Stage newStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/about.fxml"));
        newStage.setTitle("About");
        newStage.setScene(new Scene(root, 300, 100));
        newStage.setResizable(false);
        newStage.getIcons().add(new Image("img/iconOnWindow.png"));
        newStage.show();
    }

    /**
     * Opens a window of order confirmation with some additional options before the order
     * Opens an alert window if the cart is empty
     * @param actionEvent
     * @throws Exception
     */
    public void proceedToOrderClick(ActionEvent actionEvent) throws Exception {
        if(selectedMeals.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Cart is empty", ButtonType.OK).showAndWait();
            return;
        }
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/confirmOrder.fxml"));
        ConfirmOrderController controller = new ConfirmOrderController(selectedMeals,user);
        loader.setController(controller);
        newStage.setTitle("Confirmation of order");
        newStage.setScene(new Scene(loader.load(), USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        newStage.setResizable(false);
        newStage.getIcons().add(new Image("img/iconOnWindow.png"));
        newStage.show();
    }
}
