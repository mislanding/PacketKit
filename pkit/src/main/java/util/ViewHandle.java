package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import gui.ctrl.AnalysisView;
import gui.ctrl.IndexView;
import gui.ctrl.ChartView;
import gui.ctrl.bar.FilterBar;
import gui.ctrl.bar.MenuBar;
import gui.ctrl.bar.StatusBar;
import gui.ctrl.bar.ToolBar;
import gui.ctrl.View;
import gui.ctrl.browser.PacketData;
import gui.ctrl.browser.PacketHeader;
import gui.ctrl.browser.PacketList;
import gui.ctrl.list.FileList;
import gui.ctrl.list.NIFList;
import gui.model.Property;
import gui.model.SettingProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class ViewHandle {

    public static FXMLLoader GetLoader(String path) {
        FXMLLoader loader = new FXMLLoader();
        URL url = loader.getClassLoader().getResource(path);
        loader.setLocation(url);

        return loader;
    }

    public static void InitializeTop(View view) {
        IndexView indexView = (IndexView) view;
        VBox box = indexView.getTopBox();
        try {
            FXMLLoader menuBarLoader = ViewHandle.GetLoader("gui/view/bar/MenuBar.fxml");
            AnchorPane menuBarPane = menuBarLoader.load();
            menuBarPane.setMaxWidth(Double.MAX_VALUE);
            MenuBar menuBar = menuBarLoader.getController();
            menuBar.setView(view);
            indexView.setMenuBarCtrl(menuBar);
            FXMLLoader toolBarLoader = ViewHandle.GetLoader("gui/view/bar/ToolBar.fxml");
            AnchorPane toolBarPane = toolBarLoader.load();
            toolBarPane.setMaxWidth(Double.MAX_VALUE);
            ToolBar toolBar = toolBarLoader.getController();
            toolBar.setView(view);
            indexView.setToolBarCtrl(toolBar);
            FXMLLoader filterBarLoader = ViewHandle.GetLoader("gui/view/bar/FilterBar.fxml");
            AnchorPane filterBarPane = filterBarLoader.load();
            filterBarPane.setMaxWidth(Double.MAX_VALUE);
            FilterBar filterBar = filterBarLoader.getController();
            filterBar.setView(view);
            indexView.setFilterBarCtrl(filterBar);
            box.getChildren().addAll(menuBarPane, toolBarPane, filterBarPane);
            box.setSpacing(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void InitializeCenter(View view) {
        IndexView indexView = (IndexView) view;
        String type = indexView.getType();
        if (type.equals("index")) {
            try {
                VBox centerBox = ((IndexView) view).getCenterBox();
                indexView.getPane().setCenter(centerBox);
                if (centerBox.getChildren().size()>0)
                    return;
                FXMLLoader nifListLoader = ViewHandle.GetLoader("gui/view/list/NIFList.fxml");
                AnchorPane nifListPane = nifListLoader.load();
                nifListPane.setMaxWidth(Double.MAX_VALUE);
                NIFList nifList = nifListLoader.getController();
                nifList.setView(view);
                indexView.setNifListCtrl(nifList);
                FXMLLoader fileListLoader = ViewHandle.GetLoader("gui/view/list/FileList.fxml");
                AnchorPane fileListPane = fileListLoader.load();
                fileListPane.setMaxWidth(Double.MAX_VALUE);
                FileList fileList = fileListLoader.getController();
                fileList.setView(view);
                indexView.setFileListCtrl(fileList);
                centerBox.getChildren().addAll(fileListPane, nifListPane);
                centerBox.setSpacing(10);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                SplitPane browserPane = indexView.getBrowserPane();
                indexView.getPane().setCenter(browserPane);
                if (browserPane.getItems().size()>0)
                    return;
                FXMLLoader packetListLoader = ViewHandle.GetLoader("gui/view/browser/PacketList.fxml");
                AnchorPane packetListPane = packetListLoader.load();
                packetListPane.setMaxWidth(Double.MAX_VALUE);
                PacketList packetList = packetListLoader.getController();
                packetList.setView(view);
                indexView.setPacketListCtrl(packetList);
                FXMLLoader packetHeaderLoader = ViewHandle.GetLoader("gui/view/browser/PacketHeader.fxml");
                AnchorPane packetHeaderPane = packetHeaderLoader.load();
                packetHeaderPane.setMaxWidth(Double.MAX_VALUE);
                PacketHeader packetHeader = packetHeaderLoader.getController();
                packetHeader.setView(view);
                indexView.setPacketHeaderCtrl(packetHeader);
                FXMLLoader packetDataLoader = ViewHandle.GetLoader("gui/view/browser/PacketData.fxml");
                AnchorPane packetDataPane = packetDataLoader.load();
                packetDataPane.setMaxWidth(Double.MAX_VALUE);
                PacketData packetData = packetDataLoader.getController();
                packetData.setView(view);
                indexView.setPacketDataCtrl(packetData);

                browserPane.getItems().addAll(packetListPane, packetHeaderPane, packetDataPane);

                for (int i = 0; i < browserPane.getDividers().size(); i++) {
                    browserPane.getDividers().get(i).setPosition((i + 1.0) / browserPane.getItems().size());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void InitializeBottom(View view) {
        IndexView indexView = (IndexView) view;
        BorderPane pane = indexView.getPane();
        try {
            FXMLLoader statusBarLoader = ViewHandle.GetLoader("gui/view/bar/StatusBar.fxml");
            AnchorPane statusBatPane = statusBarLoader.load();
            statusBatPane.setMaxWidth(Double.MAX_VALUE);
            StatusBar statusBar = statusBarLoader.getController();
            statusBar.setView(view);
            pane.setBottom(statusBatPane);
            pane.getBottom().setLayoutX(0);
            indexView.setStatusBarCtrl(statusBarLoader.getController());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void InitializeList(String path, ListView<String> list) {
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            ObservableList<String> ob = FXCollections.observableArrayList();
            String str = null;
            int num = 0;
            while((str = br.readLine()) != null && num < SettingProperty.maxPcapFileHistory)
            {
                File file1 = new File(str);
                FileInputStream fis1 = new FileInputStream(file1);
                if (!ob.contains(str + "(" + fis1.available()/1024 + "KB)")) {
                    ob.add(str + "(" + fis1.available()/1024 + "KB)");
                    num ++;
                }

            }
            list.setItems(ob);
            fis.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void InitializeFilterComboBox(String path, ComboBox<String> box) {
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            ObservableList<String> ob = FXCollections.observableArrayList();
            String str = null;
            int num = 0;
            while((str = br.readLine()) != null && num < SettingProperty.maxFilterHistory)
            {
                if (!ob.contains(str)) {
                    ob.add(str);
                    num ++;
                }

            }
            box.setItems(ob);
            fis.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List GetPcapNIFList() {
        List pcapNifList = null;
        try {
            pcapNifList = Pcaps.findAllDevs();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }

        return pcapNifList;
    }

    public static void InitializeTable(Property property, TableView<Property> table) {
        JsonMapper mapper = new JsonMapper();
        try {
            String json = mapper.writeValueAsString(property);
            JsonNode node = mapper.readTree(json);
            if (table.getColumns().size() == 0) {
                node.fields().forEachRemaining(field -> {
                    TableColumn<Property, Object> column = new TableColumn<>();
                    column.setText(field.getKey());
                    column.setId(field.getKey());
                    column.setCellValueFactory(new PropertyValueFactory<>(field.getKey()));
                    table.getColumns().add(column);
                });
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void UpdateConfigTable(String configPath, Property property, TableView<Property> table) {
        List<Property> list = FileHandle.ReadJson(configPath, property.getClass());

        assert list != null;
        list.forEach(p -> {
                table.getItems().add(p);
            });
    }

    public static TreeView<String> InitializeMenuTree(Property property) {
        JsonMapper mapper = new JsonMapper();
        try {
            String js = mapper.writeValueAsString(property);
            System.out.println(js);
            JsonNode root = mapper.readTree(js);
            TreeItem<String> rootItem = new TreeItem<>(root.asText());
            root.fieldNames().forEachRemaining(field -> {
                TreeItem<String> item = new TreeItem<>(field);
                JsonNode node = root.path(field);
                rootItem.getChildren().add(item);
                node.fieldNames().forEachRemaining(f->{
                    TreeItem<String> it = new TreeItem<>(f);
                    item.getChildren().add(it);
                });
            });

            return new TreeView<>(rootItem);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
