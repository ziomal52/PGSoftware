package com.jopek.controller;

import com.jopek.model.Car;
import com.jopek.model.Client;
import com.jopek.model.Registry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Tomek on 2017-05-03.
 */
public class RentWindowController implements Initializable {

    @FXML
    private TableView<Client> clientsTable;
    @FXML
    private TableView<Car> carsTable;
    @FXML
    private DatePicker rentDate;
    @FXML
    private DatePicker returnDate;
    @FXML
    private AnchorPane anchorClient;
    @FXML
    private AnchorPane anchorCar;

    private Stage stage;
    private List<Registry> registries;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientsTable = loadClients();
        carsTable = loadCars();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleOk() {
        Registry registry = new Registry();

        String uniqueID = registries.stream().map(Registry::getId).max(String::compareTo).get();
        int id = Integer.valueOf(uniqueID);
        registry.setId(String.valueOf(++id));

        registry.setClientId(clientsTable.getSelectionModel().getSelectedItem().getId());
        if(carsTable.getSelectionModel().getSelectedItem().getFlag().equals("TAK")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ostrzezenie");
            alert.setContentText("Pojazd jest juz wypozyczony");
            alert.showAndWait();
        }
        else {
            registry.setCarId(carsTable.getSelectionModel().getSelectedItem().getId());
            carsTable.getSelectionModel().getSelectedItem().setFlag("TAK");
            LocalDate tmpRentDate = rentDate.getValue();
            LocalDate tmpReturnDate = returnDate.getValue();

            registry.setDate(tmpRentDate.toString());
            registry.setrDate(tmpReturnDate.toString());

            try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(getClass().getResource("/database/cars.csv").toURI()))) {
                Files.write(Paths.get(getClass().getResource("/database/history.csv").toURI()), registry.toString().getBytes(), StandardOpenOption.APPEND);

                for(Car tmpCar : carsTable.getItems()) {
                    if(tmpCar.getId().equals(carsTable.getSelectionModel().getSelectedItem().getId()))
                        tmpCar.setFlag("TAK");

                    writer.write(tmpCar.getId()+","+tmpCar.getProducer()+","+tmpCar.getModel()+","+tmpCar.getProductionYear()+
                            ","+tmpCar.getPlates()+","+tmpCar.getFlag());
                    writer.newLine();
                }

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }

            stage.close();
        }
    }

    @FXML
    private void handleClose() {
        stage.close();
    }

    private TableView<Client> loadClients() {

        try(Stream<String> stream = Files.lines(Paths.get(getClass().getResource("/database/clients.csv").toURI()))) {
            List<Client> cList = stream.map(line -> {
                String details[] = line.split(",");
                Client client = new Client();
                client.setId(details[0]);
                client.setSurname(details[1]);
                client.setName(details[2]);
                client.setCity(details[3]);
                client.setPostalCode(details[4]);
                client.setPhone(details[5]);
                client.setEmail(details[6]);

                return client;
            }).collect(Collectors.toList());

            ObservableList<Client> details = FXCollections.observableArrayList(cList);

            TableView<Client> tableView = new TableView<>();
            TableColumn<Client, String> col1 = new TableColumn<>();
            TableColumn<Client, String> col2 = new TableColumn<>();
            TableColumn<Client, String> col3 = new TableColumn<>();
            TableColumn<Client, String> col4 = new TableColumn<>();
            TableColumn<Client, String> col5 = new TableColumn<>();
            TableColumn<Client, String> col6 = new TableColumn<>();
            TableColumn<Client, String> col7 = new TableColumn<>();

            col1.setText("ID");
            col2.setText("Nazwisko");
            col3.setText("Imię");
            col4.setText("Miasto");
            col5.setText("Kod Pocztowy");
            col6.setText("Telefon");
            col7.setText("Email");

            tableView.getColumns().addAll(col1, col2, col3, col4, col5, col6, col7);
            col1.setCellValueFactory(param -> param.getValue().idProperty());
            col2.setCellValueFactory(param -> param.getValue().surnameProperty());
            col3.setCellValueFactory(param -> param.getValue().nameProperty());
            col4.setCellValueFactory(param -> param.getValue().cityProperty());
            col5.setCellValueFactory(param -> param.getValue().postalCodeProperty());
            col6.setCellValueFactory(param -> param.getValue().phoneProperty());
            col7.setCellValueFactory(param -> param.getValue().emailProperty());

            tableView.setItems(details);
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            anchorClient.getChildren().set(0, tableView);
            AnchorPane.setBottomAnchor(tableView, 0.0);
            AnchorPane.setTopAnchor(tableView, 0.0);
            AnchorPane.setLeftAnchor(tableView, 0.0);
            AnchorPane.setRightAnchor(tableView, 0.0);

            return tableView;

        }catch(IOException | URISyntaxException ioex) {
            ioex.printStackTrace();
        }

        return null;
    }

    private TableView<Car> loadCars() {

        try(Stream<String> stream = Files.lines(Paths.get(getClass().getResource("/database/cars.csv").toURI()))) {
            List<Car> cList = stream/*.filter(s -> {
                String splited[] = s.split(",");

                return splited[splited.length-1].equals("NIE");
            })*/
                    .map(line -> {
                        String details[] = line.split(",");

                        Car car = new Car();
                        car.setId(details[0]);
                        car.setProducer(details[1]);
                        car.setModel(details[2]);
                        car.setProductionYear(details[3]);
                        car.setPlates(details[4]);
                        car.setFlag(details[5]);

                        return car;
                    }).collect(Collectors.toList());

            ObservableList<Car> details = FXCollections.observableArrayList(cList);

            TableView<Car> tableView = new TableView<>();
            TableColumn<Car, String> col1 = new TableColumn<>();
            TableColumn<Car, String> col2 = new TableColumn<>();
            TableColumn<Car, String> col3 = new TableColumn<>();
            TableColumn<Car, String> col4 = new TableColumn<>();
            TableColumn<Car, String> col5 = new TableColumn<>();
            TableColumn<Car, String> col6 = new TableColumn<>();

            col1.setText("ID");
            col2.setText("Producent");
            col3.setText("Model");
            col4.setText("Rok produkcji");
            col5.setText("Rejestracja");
            col6.setText("Wypożyczony");

            tableView.getColumns().addAll(col1, col2, col3, col4, col5, col6);
            col1.setCellValueFactory(param -> param.getValue().idProperty());
            col2.setCellValueFactory(param -> param.getValue().producerProperty());
            col3.setCellValueFactory(param -> param.getValue().modelProperty());
            col4.setCellValueFactory(param -> param.getValue().productionYearProperty());
            col5.setCellValueFactory(param -> param.getValue().platesProperty());
            col6.setCellValueFactory(param -> param.getValue().flagProperty());

            tableView.setItems(details);
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            anchorCar.getChildren().set(0, tableView);
            AnchorPane.setBottomAnchor(tableView, 0.0);
            AnchorPane.setTopAnchor(tableView, 0.0);
            AnchorPane.setLeftAnchor(tableView, 0.0);
            AnchorPane.setRightAnchor(tableView, 0.0);

            return tableView;

        }catch(IOException | URISyntaxException ioex) {
            ioex.printStackTrace();
        }

        return null;
    }

    public void setRegistries(List<Registry> registries) {
        this.registries = registries;
    }
}