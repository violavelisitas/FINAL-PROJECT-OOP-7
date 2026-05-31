package ThreadHub.admin;

import ThreadHub.controller.DataStore;
import ThreadHub.model.*;
import ThreadHub.view.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardView {
  private final Stage stage;
  private final Admin admin;
  private final DataStore ds = DataStore.getInstance();  private TableView<Produk> table;
  private ObservableList<Produk> produkData;

  public AdminDashboardView(Stage stage, Admin admin) {
    this.stage = stage;
    this.admin = admin;
  }

  public void show() {
    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

    root.setLeft(buildSidebar(root));

    if (stage.getScene() != null) {
      stage.getScene().setRoot(root);
    } else {
      Scene scene = new Scene(root, 1100, 680);
      stage.setScene(scene);
      stage.centerOnScreen();
    }

    stage.setTitle(admin.getDashboardTitle());
    stage.setResizable(true);
    stage.setMaximized(true);
    stage.show();
  }

  private VBox buildSidebar(BorderPane root) {
    VBox sidebar = new VBox(6);
    sidebar.setPrefWidth(220);
    sidebar.setPadding(new Insets(28, 14, 28, 14));
    sidebar.setStyle("-fx-background-color: " + StyleKit.SIDEBAR_BG + ";");

    Label logo = new Label("🧵 ThreadHub");
    logo.setFont(Font.font(StyleKit.FONT_FAMILY, FontWeight.BOLD, 20));
    logo.setTextFill(Color.web(StyleKit.ACCENT));
    logo.setPadding(new Insets(0, 0, 10, 6));

    Label adminLabel = StyleKit.mutedLabel("Admin: " + admin.getNama());
    adminLabel.setPadding(new Insets(0, 0, 16, 6));

    Button btnProduk    = StyleKit.sidebarButton("📦 Kelola Produk");
    Button btnPengguna  = StyleKit.sidebarButton("👥 Kelola Pengguna");
    Button btnOutfit    = StyleKit.sidebarButton("📦 Kelola Outfit");
    Button btnTransaksi = StyleKit.sidebarButton("📋 Riwayat Transaksi");

    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);

    Button btnLogout = StyleKit.sidebarButton("🚪 Logout");
    btnLogout.setStyle(btnLogout.getStyle().replace(StyleKit.TEXT_PRIMARY, StyleKit.ACCENT));
    btnLogout.setOnAction(e -> logout());

    sidebar.getChildren().addAll(
      logo, adminLabel,
      StyleKit.hSeparator(),
      btnProduk, btnPengguna, btnOutfit, btnTransaksi,
      spacer,
      StyleKit.hSeparator(),
      btnLogout
    );

    btnProduk.setOnAction(e -> root.setCenter(buildContent()));
    btnPengguna.setOnAction(e -> root.setCenter(buildPenggunaPanel()));
    btnOutfit.setOnAction(e -> root.setCenter(buildOutfitPanel()));
    btnTransaksi.setOnAction(e -> root.setCenter(buildTransaksiPanel()));

    return sidebar;
  }

  private VBox buildContent() {
    VBox content = new VBox(18);
    content.setPadding(new Insets(30));
    content.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

    HBox header = new HBox(14);
    header.setAlignment(Pos.CENTER_LEFT);
    Label title = StyleKit.titleLabel("Manajemen Produk", 22);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Button btnTambah = StyleKit.primaryButton("+ Tambah Produk");
    btnTambah.setOnAction(e -> showFormDialog(null));
    header.getChildren().addAll(title, spacer, btnTambah);

    TextField searchField = new TextField();
    searchField.setPromptText("🔍 Cari produk...");
    searchField.setPrefWidth(300);
    searchField.setStyle(
      "-fx-background-color: " + StyleKit.CARD_BG + ";" +
      "-fx-text-fill: " + StyleKit.TEXT_PRIMARY + ";" +
      "-fx-prompt-text-fill: " + StyleKit.TEXT_MUTED + ";" +
      "-fx-border-color: " + StyleKit.BORDER + ";" +
      "-fx-border-radius: 8; -fx-background-radius: 8;" +
      "-fx-padding: 8 12; -fx-font-size: 13px;"
    );
    searchField.textProperty().addListener((obs, o, nv) -> filterTable(nv));

    table = buildTable();
    VBox.setVgrow(table, Priority.ALWAYS);

    content.getChildren().addAll(header, searchField, table);
    return content;
  }

  @SuppressWarnings("unchecked")
  private TableView<Produk> buildTable() {
    produkData = FXCollections.observableArrayList(ds.getAllProduk());
    TableView<Produk> tv = new TableView<>(produkData);
    tv.setStyle(
      "-fx-background-color: " + StyleKit.CARD_BG + ";" +
      "-fx-text-fill: " + StyleKit.TEXT_PRIMARY + ";" +
      "-fx-border-color: " + StyleKit.BORDER + ";" +
      "-fx-border-radius: 10; -fx-background-radius: 10;"
    );
    tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    TableColumn<Produk, Void> colNo = new TableColumn<>("No");
    colNo.setPrefWidth(40);
    colNo.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
        } else {
          setText(String.valueOf(getIndex() + 1));
          setTextFill(Color.web("#222222"));
          setAlignment(Pos.CENTER);
        }
      }
    });

    TableColumn<Produk, String> colNama = col("Nama","nama",180);
    TableColumn<Produk, String> colGender = col("Gender","gender",90);
    TableColumn<Produk, String> colKategori = col("Kategori","kategori",100);
    TableColumn<Produk, String> colUkuran = col("Ukuran","ukuran",70);
    TableColumn<Produk, String> colWarna = col("Warna","warna",90);
    TableColumn<Produk, Integer> colStok = col("Stok","stok",60);

    TableColumn<Produk, Double> colHarga = new TableColumn<>("Harga");
    colHarga.setPrefWidth(120);
    colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
    colHarga.setCellFactory(c -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("Rp %,.0f", v));
        setTextFill(Color.web("#222222"));
      }
    });

    TableColumn<Produk, Void> colAksi = new TableColumn<>("Aksi");
    colAksi.setPrefWidth(130);
    colAksi.setCellFactory(c -> new TableCell<>() {
      private final Button btnEdit  = StyleKit.outlineButton("Edit");
      private final Button btnHapus = new Button("Hapus");
      private final HBox box = new HBox(8, btnEdit, btnHapus);
      {
        box.setAlignment(Pos.CENTER);
        btnHapus.setStyle(
          "-fx-background-color: " + StyleKit.ACCENT + ";" +
          "-fx-text-fill: white; -fx-font-size: 12px;" +
          "-fx-padding: 6 14; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnEdit.setOnAction(e -> showFormDialog(getTableView().getItems().get(getIndex())));
        btnHapus.setOnAction(e -> hapusProduk(getTableView().getItems().get(getIndex())));
      }
      @Override protected void updateItem(Void v, boolean empty) {
        super.updateItem(v, empty);
        setGraphic(empty ? null : box);
      }
    });

    tv.getColumns().addAll(colNo, colNama, colGender, colKategori, colUkuran, colWarna, colStok, colHarga, colAksi);
    return tv;
  }

  private <T> TableColumn<Produk, T> col(String title, String field, double width) {
    TableColumn<Produk, T> col = new TableColumn<>(title);
    col.setPrefWidth(width);
    col.setCellValueFactory(new PropertyValueFactory<>(field));
    col.setCellFactory(c -> new TableCell<>() {
      @Override protected void updateItem(T v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : v.toString());
        setTextFill(Color.web("#222222"));
        setStyle("-fx-background-color: transparent;");
      }
    });
    return col;
  }

  private void filterTable(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      produkData.setAll(ds.getAllProduk());
    } else {
      produkData.setAll(ds.cariProduk(keyword));
    }
  }

  private void showFormDialog(Produk existing) {
    Stage dialog = new Stage();
    dialog.setTitle(existing == null ? "Tambah Produk" : "Edit Produk");
    dialog.initOwner(stage);

    VBox form = new VBox(14);
    form.setPadding(new Insets(30));
    form.setPrefWidth(420);
    form.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

    Label title = StyleKit.titleLabel(existing == null ? "Tambah Produk Baru" : "Edit Produk", 20);
    TextField tfNama = dialogField("Nama Produk", existing != null ? existing.getNama() : "");
        
    ComboBox<String> cbGender = new ComboBox<>();
    cbGender.getItems().addAll("PRIA", "WANITA", "ANAK-ANAK");
    cbGender.setPrefWidth(Double.MAX_VALUE);
    cbGender.setStyle(
      "-fx-background-color: " + StyleKit.CARD_BG + ";" +
        "-fx-border-color: " + StyleKit.BORDER + ";" +
        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4 8;"
    );
        
    cbGender.setButtonCell(new ListCell<>() {
      @Override protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        setTextFill(Color.web(StyleKit.TEXT_CONTRAST));
        setStyle("-fx-background-color: transparent;");
        }
    });
    cbGender.setCellFactory(lv -> new ListCell<>() {
      @Override protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        setTextFill(Color.web(StyleKit.TEXT_CONTRAST));
        setStyle("-fx-background-color: " + StyleKit.CARD_BG + ";");
      }
    });

    cbGender.setValue(existing != null && existing.getGender() != null ? existing.getGender().toUpperCase() : "PRIA");
        
    VBox genderBox = new VBox(5, new Label("Gender") {{ setTextFill(Color.web(StyleKit.TEXT_MUTED)); setFont(Font.font(StyleKit.FONT_FAMILY, 12)); }}, cbGender);

    final String[] selectedImagePath = { existing != null ? existing.getImagePath() : null };
    Button btnUpload = StyleKit.outlineButton("Pilih Foto...");
    btnUpload.setStyle("-fx-font-size: 11px; -fx-padding: 6 12;");
    Label lblFoto = new Label(selectedImagePath[0] == null ? "Belum ada foto" : "Foto terpilih");
    lblFoto.setTextFill(Color.web(StyleKit.TEXT_MUTED));
        
    btnUpload.setOnAction(e -> {
      FileChooser fc = new FileChooser();
      fc.setTitle("Pilih Foto Produk");
      fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
      File file = fc.showOpenDialog(dialog);
      if (file != null) {
        selectedImagePath[0] = file.toURI().toString(); 
        lblFoto.setText(file.getName());
      }
    });
    HBox fotoBox = new HBox(12, btnUpload, lblFoto);
    fotoBox.setAlignment(Pos.CENTER_LEFT);

    TextField tfKategori  = dialogField("Kategori", existing != null ? existing.getKategori() : "");
    TextField tfUkuran    = dialogField("Ukuran",       existing != null ? existing.getUkuran() : "");
    TextField tfWarna     = dialogField("Warna",        existing != null ? existing.getWarna() : "");
    TextField tfHarga     = dialogField("Harga (Rp)",   existing != null ? String.valueOf((int)existing.getHarga()) : "");
    TextField tfStok      = dialogField("Stok",         existing != null ? String.valueOf(existing.getStok()) : "");
        
    TextArea taDeskripsi = new TextArea(existing != null ? existing.getDeskripsi() : "");
    taDeskripsi.setPromptText("Deskripsi produk");
    taDeskripsi.setPrefRowCount(3);
    taDeskripsi.setWrapText(true);
    taDeskripsi.setStyle(
        "-fx-control-inner-background: " + StyleKit.CARD_BG + ";" +
        "-fx-background-color: " + StyleKit.BORDER + ", " + StyleKit.CARD_BG + ";" +
        "-fx-background-insets: 0, 1;" +
        "-fx-text-fill: " + StyleKit.TEXT_CONTRAST + ";" +
        "-fx-prompt-text-fill: " + StyleKit.TEXT_MUTED + ";" +
        "-fx-border-radius: 8; -fx-background-radius: 8;"
    );

    Label errLabel = new Label("");
    errLabel.setTextFill(Color.web(StyleKit.ACCENT));

    Button btnSimpan = StyleKit.primaryButton("Simpan");
    btnSimpan.setMaxWidth(Double.MAX_VALUE);
    btnSimpan.setOnAction(e -> {
        try {
          String nama      = tfNama.getText().trim();
          String gender    = cbGender.getValue();
          String kategori  = tfKategori.getText().trim();
          String ukuran    = tfUkuran.getText().trim();
          String warna     = tfWarna.getText().trim();
          String deskripsi = taDeskripsi.getText().trim();
          double harga     = Double.parseDouble(tfHarga.getText().trim());
          int stok         = Integer.parseInt(tfStok.getText().trim());
          String imgPath   = selectedImagePath[0];
          if (nama.isEmpty() || kategori.isEmpty()) {
            errLabel.setText("Nama dan kategori wajib diisi.");
              return;
          }
                
        if (existing == null) {
          Produk baru = new Produk(ds.generateProdukId(), nama, deskripsi, kategori, harga, stok, ukuran, warna, gender, imgPath);
          ds.tambahProduk(baru);
        } else {
          existing.setNama(nama);
          existing.setGender(gender);
          existing.setKategori(kategori);
          existing.setUkuran(ukuran);
          existing.setWarna(warna);
          existing.setDeskripsi(deskripsi);
          existing.setHarga(harga);
          existing.setStok(stok);
          existing.setImagePath(imgPath); 
                    
          ds.simpanDataProduk(); 
        }
        produkData.setAll(ds.getAllProduk());
        dialog.close();
      } catch (NumberFormatException ex) {
        errLabel.setText("Harga dan stok harus berupa angka.");
      }
    });

    form.getChildren().addAll(
      title, StyleKit.hSeparator(),
      tfNama, genderBox, 
      new Label("Foto Produk") {{ setTextFill(Color.web(StyleKit.TEXT_MUTED)); setFont(Font.font(StyleKit.FONT_FAMILY, 12)); }},
      fotoBox, tfKategori, tfUkuran, tfWarna,
      new Label("Deskripsi") {{ setTextFill(Color.web(StyleKit.TEXT_MUTED)); setFont(Font.font(StyleKit.FONT_FAMILY, 12)); }},
      taDeskripsi, tfHarga, tfStok, errLabel, btnSimpan
    );

    ScrollPane scrollPane = new ScrollPane(form);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background: " + StyleKit.DARK_BG + "; -fx-border-color: transparent;");

    dialog.setScene(new Scene(scrollPane, 440, 700));
    dialog.showAndWait();
  }
  
  private void showFormDialog(Produk existing) {}
  private void hapusProduk(Produk p) {}
  private VBox buildPenggunaPanel() { return new VBox(); }
  private VBox buildOutfitPanel() { return new VBox(); }
  private VBox buildTransaksiPanel() { return new VBox(); }
  private void logout() {}
}