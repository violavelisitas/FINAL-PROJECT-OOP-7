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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardView {

    private final Stage stage;
    private final Admin admin;
    private final DataStore ds = DataStore.getInstance();

    private TableView<Produk> table;
    private ObservableList<Produk> produkData;

    public AdminDashboardView(Stage stage, Admin admin) {
        this.stage = stage;
        this.admin = admin;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");
        root.setLeft(buildSidebar(root));
        root.setCenter(buildContent());

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

        Label logo = new Label("THREADHUB");
        logo.setFont(Font.font(StyleKit.FONT_FAMILY, FontWeight.EXTRA_BOLD, 22));
        logo.setTextFill(Color.web(StyleKit.ACCENT));
        logo.setPadding(new Insets(0, 0, 10, 6));

        Label adminLabel = StyleKit.mutedLabel("Admin: " + admin.getNama());
        adminLabel.setPadding(new Insets(0, 0, 16, 6));

        Button btnProduk     = StyleKit.sidebarButton("📦  Kelola Produk");
        Button btnPengguna   = StyleKit.sidebarButton("👥  Kelola Pengguna"); 
        Button btnOutfit     = StyleKit.sidebarButton("📦  Kelola Outfit"); 
        Button btnTransaksi  = StyleKit.sidebarButton("📋  Riwayat Transaksi");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = StyleKit.sidebarButton("🚪  Logout");
        btnLogout.setStyle(btnLogout.getStyle().replace(StyleKit.TEXT_PRIMARY, StyleKit.ACCENT));
        btnLogout.setOnAction(e -> logout());

        sidebar.getChildren().addAll(logo, adminLabel, StyleKit.hSeparator(), btnProduk, btnPengguna, btnOutfit, btnTransaksi, spacer, StyleKit.hSeparator(), btnLogout);

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
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-font-size: 13px;"
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
        tv.setStyle("-fx-background-color: " + StyleKit.CARD_BG + "; -fx-text-fill: " + StyleKit.TEXT_PRIMARY + "; -fx-border-color: " + StyleKit.BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Produk, Void> colNo = new TableColumn<>("No");
        colNo.setPrefWidth(40);
        colNo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setText(null);
                else { setText(String.valueOf(getIndex() + 1)); setTextFill(Color.web("#222222")); setAlignment(Pos.CENTER); }
            }
        });

        TableColumn<Produk, String>  colNama     = col("Nama",      "nama",      180);
        TableColumn<Produk, String>  colGender   = col("Gender",    "gender",    90); 
        TableColumn<Produk, String>  colKategori = col("Kategori",  "kategori",  100);
        TableColumn<Produk, String>  colUkuran   = col("Ukuran",    "ukuran",    70);
        TableColumn<Produk, String>  colWarna    = col("Warna",     "warna",     90);
        TableColumn<Produk, Integer> colStok     = col("Stok",      "stok",      60);

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
            private final Button btnEdit = StyleKit.outlineButton("Edit");
            private final Button btnHapus = new Button("Hapus");
            private final HBox box = new HBox(8, btnEdit, btnHapus);
            {
                box.setAlignment(Pos.CENTER);
                btnHapus.setStyle("-fx-background-color: " + StyleKit.ACCENT + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 8; -fx-cursor: hand;");
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
        if (keyword == null || keyword.isBlank()) produkData.setAll(ds.getAllProduk());
        else produkData.setAll(ds.cariProduk(keyword));
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
        cbGender.setStyle("-fx-background-color: " + StyleKit.CARD_BG + "; -fx-border-color: " + StyleKit.BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4 8;");
        
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
        taDeskripsi.setStyle("-fx-control-inner-background: " + StyleKit.CARD_BG + "; -fx-background-color: " + StyleKit.BORDER + ", " + StyleKit.CARD_BG + "; -fx-background-insets: 0, 1; -fx-text-fill: " + StyleKit.TEXT_CONTRAST + "; -fx-prompt-text-fill: " + StyleKit.TEXT_MUTED + "; -fx-border-radius: 8; -fx-background-radius: 8;");

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

                if (nama.isEmpty() || kategori.isEmpty()) { errLabel.setText("Nama dan kategori wajib diisi."); return; }
                
                if (existing == null) {
                    Produk baru = new Produk(ds.generateProdukId(), nama, deskripsi, kategori, harga, stok, ukuran, warna, gender, imgPath);
                    ds.tambahProduk(baru);
                } else {
                    existing.setNama(nama); existing.setGender(gender); existing.setKategori(kategori);
                    existing.setUkuran(ukuran); existing.setWarna(warna); existing.setDeskripsi(deskripsi);
                    existing.setHarga(harga); existing.setStok(stok); existing.setImagePath(imgPath); 
                    ds.simpanDataProduk(); 
                }
                produkData.setAll(ds.getAllProduk());
                dialog.close();
            } catch (NumberFormatException ex) {
                errLabel.setText("Harga dan stok harus berupa angka.");
            }
        });

        form.getChildren().addAll(
                title, StyleKit.hSeparator(), tfNama, genderBox, 
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

    private TextField dialogField(String prompt, String value) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: " + StyleKit.CARD_BG + "; -fx-text-fill: " + StyleKit.TEXT_CONTRAST + "; -fx-border-color: " + StyleKit.BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9 12; -fx-font-size: 13px; -fx-prompt-text-fill: " + StyleKit.TEXT_MUTED + ";");
        return tf;
    }

    private void hapusProduk(Produk p) {
        showConfirmationDialog("Hapus Produk", "Yakin ingin menghapus " + p.getNama() + "?", "Tindakan ini tidak dapat dibatalkan.", () -> {
            ds.hapusProduk(p.getId());
            produkData.setAll(ds.getAllProduk());
        });
    }

    private VBox buildPenggunaPanel() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = StyleKit.titleLabel("Manajemen Pengguna", 22);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnTambah = StyleKit.primaryButton("+ Tambah Pengguna");
        btnTambah.setOnAction(e -> showFormUserDialog(null));
        header.getChildren().addAll(title, spacer, btnTambah);

        ObservableList<User> userData = FXCollections.observableArrayList(ds.getAllUsers());
        TableView<User> tv = new TableView<>(userData);
        tv.setStyle("-fx-background-color: " + StyleKit.CARD_BG + "; -fx-text-fill: " + StyleKit.TEXT_PRIMARY + "; -fx-border-color: " + StyleKit.BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tv, Priority.ALWAYS);

        TableColumn<User, String> colNamaColumn = new TableColumn<>("Nama");
        colNamaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNamaColumn.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : v); setTextFill(Color.web("#222222"));
            }
        });

        TableColumn<User, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUsername.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : v); setTextFill(Color.web("#222222"));
            }
        });

        TableColumn<User, String> colRole = new TableColumn<>("Role");
        colRole.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setText(null);
                else {
                    User u = getTableRow().getItem();
                    setText(u instanceof Admin ? "Administrator" : "Buyer");
                    setTextFill(Color.web("#222222"));
                }
            }
        });

        TableColumn<User, Void> colAksi = new TableColumn<>("Aksi");
        colAksi.setPrefWidth(80);
        colAksi.setCellFactory(c -> new TableCell<>() {
            private final Button btnHapus = new Button("Hapus");
            {
                btnHapus.setStyle("-fx-background-color: " + StyleKit.ACCENT + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");
                btnHapus.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    
                    if (u.getUsername().equals("admin")) {
                        showErrorDialog("Akses Ditolak", "Akun Super Admin tidak bisa dihapus!");
                        return;
                    }
                    
                    showConfirmationDialog(
                        "Konfirmasi Hapus Pengguna", 
                        "Yakin ingin menghapus akun '" + u.getNama() + "'?", 
                        "Seluruh data pengguna ini akan hilang dan tidak dapat dikembalikan.", 
                        () -> {
                            ds.hapusUser(u.getId());
                            userData.setAll(ds.getAllUsers());
                        }
                    );
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnHapus);
                setAlignment(Pos.CENTER);
            }
        });

        tv.getColumns().addAll(colNamaColumn, colUsername, colRole, colAksi);
        content.getChildren().addAll(header, tv);
        return content;
    }

    private void showFormUserDialog(User existing) {
        Stage dialog = new Stage();
        dialog.setTitle("Tambah Pengguna Baru");
        dialog.initOwner(stage);

        VBox form = new VBox(14);
        form.setPadding(new Insets(30));
        form.setPrefWidth(350);
        form.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

        Label title = StyleKit.titleLabel("Tambah Pengguna", 20);
        TextField tfNama = dialogField("Nama Lengkap", "");
        TextField tfUsername = dialogField("Username", "");
        TextField tfPassword = dialogField("Password", "");
        
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Buyer", "Admin");
        cbRole.setValue("Buyer");
        cbRole.setPrefWidth(Double.MAX_VALUE);

        Label errLabel = new Label("");
        errLabel.setTextFill(Color.web(StyleKit.ACCENT));

        Button btnSimpan = StyleKit.primaryButton("Simpan Akun");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        btnSimpan.setOnAction(e -> {
            String nama = tfNama.getText().trim();
            String username = tfUsername.getText().trim();
            String password = tfPassword.getText().trim();
            String role = cbRole.getValue();

            if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) { errLabel.setText("Semua kolom wajib diisi."); return; }
            int newId = ds.generateUserId();
            User newUser = role.equals("Admin") ? new Admin(newId, username, password, nama) : new Buyer(newId, username, password, nama);
            ds.tambahUser(newUser);
            show(); 
            dialog.close();
        });

        form.getChildren().addAll(title, StyleKit.hSeparator(), tfNama, tfUsername, tfPassword, new Label("Role/Peran:") {{ setTextFill(Color.web(StyleKit.TEXT_MUTED)); }}, cbRole, errLabel, btnSimpan);

        dialog.setScene(new Scene(form));
        dialog.showAndWait();
    }

    private VBox buildOutfitPanel() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = StyleKit.titleLabel("Manajemen Outfit Bundle", 22);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnTambah = StyleKit.primaryButton("+ Buat Paket Outfit");
        btnTambah.setOnAction(e -> showFormOutfitDialog(null));
        header.getChildren().addAll(title, spacer, btnTambah);

        ObservableList<OutfitBundle> outfitData = FXCollections.observableArrayList(ds.getAllOutfit());
        TableView<OutfitBundle> tv = new TableView<>(outfitData);
        tv.setStyle("-fx-background-color: " + StyleKit.CARD_BG + "; -fx-text-fill: " + StyleKit.TEXT_PRIMARY + "; -fx-border-color: " + StyleKit.BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tv, Priority.ALWAYS);

        TableColumn<OutfitBundle, String> colNama = new TableColumn<>("Nama Paket");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaPaket"));
        
        TableColumn<OutfitBundle, String> colStyle = new TableColumn<>("Style");
        colStyle.setCellValueFactory(new PropertyValueFactory<>("kategoriStyle"));
        colStyle.setPrefWidth(100);

        TableColumn<OutfitBundle, Void> colTotal = new TableColumn<>("Total Harga (1 Set)");
        colTotal.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setText(null);
                else setText(getTableRow().getItem().getTotalHargaFormatted());
                setTextFill(Color.web("#222222"));
            }
        });

        TableColumn<OutfitBundle, Void> colAksi = new TableColumn<>("Aksi");
        colAksi.setPrefWidth(130);
        colAksi.setCellFactory(c -> new TableCell<>() {
            private final Button btnEdit = StyleKit.outlineButton("Edit");
            private final Button btnHapus = new Button("Hapus");
            private final HBox box = new HBox(8, btnEdit, btnHapus);
            {
                box.setAlignment(Pos.CENTER);
                btnHapus.setStyle("-fx-background-color: " + StyleKit.ACCENT + "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");
                btnEdit.setOnAction(e -> showFormOutfitDialog(getTableView().getItems().get(getIndex())));
                btnHapus.setOnAction(e -> {
                    OutfitBundle ob = getTableView().getItems().get(getIndex());
                    ds.getAllOutfit().remove(ob);
                    ds.simpanDataOutfit();
                    outfitData.setAll(ds.getAllOutfit());
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
                setAlignment(Pos.CENTER);
            }
        });

        tv.getColumns().addAll(colNama, colStyle, colTotal, colAksi);
        content.getChildren().addAll(header, tv);
        return content;
    }

    private void showFormOutfitDialog(OutfitBundle existing) {
        Stage dialog = new Stage();
        dialog.setTitle(existing == null ? "Buat Paket Outfit Baru" : "Edit Paket Outfit");
        dialog.initOwner(stage);

        VBox form = new VBox(14);
        form.setPadding(new Insets(30));
        form.setPrefWidth(420); 
        form.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

        Label title = StyleKit.titleLabel(existing == null ? "Paket Outfit Baru" : "Edit Paket Outfit", 20);
        TextField tfNama = dialogField("Nama Paket (Cth: Streetwear Keren)", existing != null ? existing.getNamaPaket() : "");
        
        ComboBox<String> cbStyle = new ComboBox<>();
        cbStyle.getItems().addAll("STREETWEAR", "CASUAL", "FORMAL", "SPORTY");
        cbStyle.setValue(existing != null ? existing.getKategoriStyle() : "STREETWEAR");
        cbStyle.setPrefWidth(Double.MAX_VALUE);

        final String[] selectedImagePath = { existing != null ? existing.getImagePath() : null };
        Button btnUpload = StyleKit.outlineButton("Pilih Foto Referensi Outfit...");
        btnUpload.setStyle("-fx-font-size: 11px; -fx-padding: 6 12;");
        Label lblFoto = new Label(selectedImagePath[0] == null ? "Belum ada foto" : "Foto terpilih");
        lblFoto.setTextFill(Color.web(StyleKit.TEXT_MUTED));
        
        btnUpload.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Pilih Foto Referensi Outfit");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fc.showOpenDialog(dialog);
            if (file != null) {
                selectedImagePath[0] = file.toURI().toString(); 
                lblFoto.setText(file.getName());
            }
        });
        HBox fotoBox = new HBox(12, btnUpload, lblFoto);
        fotoBox.setAlignment(Pos.CENTER_LEFT);

        TextArea taDeskripsi = new TextArea(existing != null ? existing.getDeskripsi() : "");
        taDeskripsi.setPromptText("Deskripsi Outfit...");
        taDeskripsi.setPrefRowCount(2);
        
        Label lblPilih = new Label("Pilih Produk untuk Paket Ini:");
        lblPilih.setTextFill(Color.web(StyleKit.TEXT_MUTED));
        
        VBox productSelector = new VBox(8);
        productSelector.setStyle("-fx-background-color: " + StyleKit.CARD_BG + "; -fx-padding: 10; -fx-background-radius: 5;");
        List<CheckBox> listCheckboxes = new ArrayList<>();
        
        for (Produk p : ds.getAllProduk()) {
            CheckBox cb = new CheckBox(p.getNama() + " (" + p.getHargaFormatted() + ")");
            cb.setTextFill(Color.web(StyleKit.TEXT_CONTRAST));
            cb.setUserData(p); 
            if (existing != null) {
                boolean isSelected = existing.getListProduk().stream().anyMatch(ep -> ep.getId() == p.getId());
                cb.setSelected(isSelected);
            }
            productSelector.getChildren().add(cb);
            listCheckboxes.add(cb);
        }

        ScrollPane scrollProduk = new ScrollPane(productSelector);
        scrollProduk.setPrefHeight(150);
        scrollProduk.setFitToWidth(true);

        Label errLabel = new Label("");
        errLabel.setTextFill(Color.web(StyleKit.ACCENT));

        Button btnSimpan = StyleKit.primaryButton(existing == null ? "Simpan Paket Baru" : "Simpan Perubahan");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        btnSimpan.setOnAction(e -> {
            String nama = tfNama.getText().trim();
            String style = cbStyle.getValue();
            String deskripsi = taDeskripsi.getText().trim();
            String imgPath = selectedImagePath[0]; 

            if (nama.isEmpty()) { errLabel.setText("Nama paket tidak boleh kosong."); return; }
            boolean hasItem = listCheckboxes.stream().anyMatch(CheckBox::isSelected);
            if (!hasItem) { errLabel.setText("Pilih minimal 1 produk untuk paket ini."); return; }

            if (existing == null) {
                int newId = ds.getAllOutfit().size() + 1;
                OutfitBundle newBundle = new OutfitBundle(newId, nama, style, deskripsi, imgPath);
                for (CheckBox cb : listCheckboxes) { if (cb.isSelected()) newBundle.tambahProdukKePaket((Produk) cb.getUserData()); }
                ds.getAllOutfit().add(newBundle);
            } else {
                existing.setNamaPaket(nama); existing.setKategoriStyle(style);
                existing.setDeskripsi(deskripsi); existing.setImagePath(imgPath);
                existing.getListProduk().clear(); 
                for (CheckBox cb : listCheckboxes) { if (cb.isSelected()) existing.tambahProdukKePaket((Produk) cb.getUserData()); }
            }
            ds.simpanDataOutfit(); show(); dialog.close();
        });

        form.getChildren().addAll(title, StyleKit.hSeparator(), tfNama, cbStyle, fotoBox, taDeskripsi, lblPilih, scrollProduk, errLabel, btnSimpan);
        dialog.setScene(new Scene(form));
        dialog.showAndWait();
    }

    private VBox buildTransaksiPanel() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: " + StyleKit.DARK_BG + ";");

        Label title = StyleKit.titleLabel("Riwayat Semua Transaksi", 22);
        
        ListView<Transaksi> list = new ListView<>();
        list.setStyle("-fx-background-color: " + StyleKit.CARD_BG + "; -fx-border-color: " + StyleKit.BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10;");
        VBox.setVgrow(list, Priority.ALWAYS);

        var transaksiList = ds.getAllTransaksi();
        if (!transaksiList.isEmpty()) list.getItems().addAll(transaksiList);

        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Transaksi t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) setText(null);
                else {
                    setText(t.getRingkasan() + "  |  Buyer: " + t.getBuyer().getNama());
                    setTextFill(Color.web(StyleKit.TEXT_PRIMARY));
                    setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-padding: 6px 10px;");
                }
            }
        });

        list.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && list.getSelectionModel().getSelectedItem() != null) {
                showDetailTransaksiDialog(list.getSelectionModel().getSelectedItem());
            }
        });

        Label hintLabel = new Label("💡 Klik ganda (double-click) pada baris transaksi untuk melihat detail barang yang dibeli.");
        hintLabel.setTextFill(Color.web(StyleKit.TEXT_MUTED));
        hintLabel.setFont(Font.font(StyleKit.FONT_FAMILY, 12));

        content.getChildren().addAll(title, hintLabel, list);
        return content;
    }

    private void showConfirmationDialog(String titleText, String headerText, String contentText, Runnable onConfirm) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1c29; -fx-background-radius: 15; -fx-border-color: #2d3142; -fx-border-radius: 15; -fx-border-width: 2;");

        Label icon = new Label("❓");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");

        Label header = new Label(headerText);
        header.setStyle("-fx-text-fill: #a9b1d6; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label content = new Label(contentText);
        content.setStyle("-fx-text-fill: #8e95b3; -fx-font-size: 13px;");
        content.setTextAlignment(TextAlignment.CENTER);

        Button btnYes = StyleKit.primaryButton("Ya, Hapus");
        btnYes.setStyle(btnYes.getStyle() + "-fx-background-color: #e06c75; -fx-text-fill: white;"); 
        btnYes.setOnAction(e -> {
            onConfirm.run();
            dialog.close();
        });

        Button btnNo = StyleKit.outlineButton("Batal");
        btnNo.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(15, btnNo, btnYes);
        btnBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(icon, title, header, content, btnBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    private void showErrorDialog(String titleText, String msg) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1c29; -fx-background-radius: 15; -fx-border-color: #2d3142; -fx-border-radius: 15; -fx-border-width: 2;");

        Label icon = new Label("❌");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: #e06c75; -fx-font-weight: bold; -fx-font-size: 20px;");

        Label content = new Label(msg);
        content.setStyle("-fx-text-fill: #a9b1d6; -fx-font-size: 14px;");
        content.setTextAlignment(TextAlignment.CENTER);
        content.setWrapText(true);

        Button btnOk = StyleKit.primaryButton("Mengerti");
        btnOk.setMinWidth(120);
        btnOk.setOnAction(e -> dialog.close());

        root.getChildren().addAll(icon, title, content, btnOk);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    private void showDetailTransaksiDialog(Transaksi t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1c29; -fx-background-radius: 15; -fx-border-color: #2d3142; -fx-border-radius: 15; -fx-border-width: 2;");

        Label title = new Label("Detail Transaksi");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");

        Label header = new Label("Informasi Transaksi " + String.format("#TRX-%04d", t.getId()));
        header.setStyle("-fx-text-fill: #a9b1d6; -fx-font-size: 14px;");

        StringBuilder detail = new StringBuilder();
        detail.append("Waktu\t\t: ").append(t.getWaktuFormatted()).append("\n");
        detail.append("Pembeli\t: ").append(t.getBuyer().getNama()).append("\n");
        detail.append("Status\t\t: ").append(t.getStatus()).append("\n");
        detail.append("----------------------------------------------------------------------\n");
        detail.append("Daftar Barang yang Dibeli:\n\n");

        for (ItemKeranjang item : t.getItems()) {
            detail.append(" • ").append(item.getProduk().getNama()).append("\n")
                  .append("   Jumlah: ").append(item.getJumlah()).append(" x ")
                  .append(String.format("Rp %,.0f", item.getProduk().getHarga())).append("\n")
                  .append("   Subtotal: ").append(item.getSubtotalFormatted()).append("\n\n");
        }

        detail.append("----------------------------------------------------------------------\n");
        detail.append("TOTAL BAYAR\t: ").append(t.getTotalFormatted());

        TextArea area = new TextArea(detail.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(420);
        area.setPrefHeight(300);
        area.setStyle("-fx-control-inner-background: #24283b; -fx-background-color: #24283b; -fx-text-fill: #c0caf5; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-border-color: #414868; -fx-border-radius: 8; -fx-background-radius: 8;");

        Button btnOk = StyleKit.primaryButton("Tutup");
        btnOk.setMinWidth(120);
        btnOk.setOnAction(e -> dialog.close());

        root.getChildren().addAll(title, header, area, btnOk);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    private void logout() {
        new ThreadHub.view.LoginView(stage).show();
    }
}