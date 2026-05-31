package ThreadHub.buyer;

import ThreadHub.controller.KeranjangController;
import ThreadHub.model.*;
import ThreadHub.view.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

public class KeranjangView {

    private final BuyerDashboardView dashboard;
    private final KeranjangController keranjang;
    
    private Label lblSubtotalValue;
    private Label lblDiskonValue;
    private Label lblPpnValue;
    private Label lblPphValue;
    private Label totalValueLabel;
    private HBox rowDisc;
    private HBox rowPph;

    public KeranjangView(BuyerDashboardView dashboard, KeranjangController keranjang) {
        this.dashboard = dashboard;
        this.keranjang = keranjang;
    }

    public VBox build() {
        // --- 1. INISIALISASI VARIABEL UI TERLEBIH DAHULU ---
        lblSubtotalValue = new Label();
        lblSubtotalValue.setStyle("-fx-text-fill: white;");

        lblDiskonValue = new Label(); 
        lblDiskonValue.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        lblPpnValue = new Label();
        lblPpnValue.setStyle("-fx-text-fill: white;");

        lblPphValue = new Label();
        lblPphValue.setStyle("-fx-text-fill: white;");

        totalValueLabel = new Label();
        totalValueLabel.setStyle("-fx-text-fill: " + StyleKit.ACCENT + "; -fx-font-weight: bold; -fx-font-size: 24px;");

        rowDisc = new HBox();
        rowPph = new HBox();

        // --- 2. MEMBANGUN LAYOUT UTAMA ---
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #FFFFFF;");

        Label title = StyleKit.titleLabel("🛒 Keranjang Belanja", 22);
        title.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 22px;");

        if (keranjang.isEmpty()) {
            Label kosong = new Label("Keranjang kosong. Yuk belanja dulu!");
            kosong.setStyle("-fx-text-fill: gray; -fx-font-size: 16px;");
            content.getChildren().addAll(title, kosong);
            return content;
        }

        VBox itemList = new VBox(12);
        for (ItemKeranjang item : keranjang.getItems()) {
            itemList.getChildren().add(buildItemRow(item));
        }

        ScrollPane scroll = new ScrollPane(itemList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scroll.setPrefHeight(360);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // --- 3. MEMBANGUN PANEL RINGKASAN BELANJA ---
        VBox summary = StyleKit.card(20);
        summary.setSpacing(12);
        
        Label titleSum = new Label("Ringkasan Belanja");
        titleSum.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        HBox rowSub = new HBox();
        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
        Label lblSubTeks = new Label("Subtotal");
        lblSubTeks.setStyle("-fx-text-fill: #eaeaea;");
        rowSub.getChildren().addAll(lblSubTeks, spacer1, lblSubtotalValue);

        Label lblDiscTeks = new Label("Diskon (5%)"); 
        lblDiscTeks.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);
        rowDisc.getChildren().addAll(lblDiscTeks, spacer2, lblDiskonValue);

        HBox rowPpn = new HBox();
        Region spacer3 = new Region(); HBox.setHgrow(spacer3, Priority.ALWAYS);
        Label lblPpnTeks = new Label("PPN (11%)");
        lblPpnTeks.setStyle("-fx-text-fill: #eaeaea;");
        rowPpn.getChildren().addAll(lblPpnTeks, spacer3, lblPpnValue);

        Region spacer4 = new Region(); HBox.setHgrow(spacer4, Priority.ALWAYS);
        Label lblPphTeks = new Label("PPh (1.5%)");
        lblPphTeks.setStyle("-fx-text-fill: #eaeaea;");
        rowPph.getChildren().addAll(lblPphTeks, spacer4, lblPphValue);

        Separator sep = new Separator();

        Label totalLabel = new Label("Total Bayar");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: white;");
        totalLabel.setAlignment(Pos.CENTER_LEFT);

        HBox rowTotal = new HBox(); 
        rowTotal.setAlignment(Pos.CENTER);
        Region spacer5 = new Region(); HBox.setHgrow(spacer5, Priority.ALWAYS);
        
        rowTotal.getChildren().addAll(totalLabel, spacer5, totalValueLabel);

        Button btnCheckout = StyleKit.primaryButton("Bayar Sekarang →");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);
        btnCheckout.setOnAction(e -> doCheckout());

        Button btnKosongkan = StyleKit.outlineButton("Kosongkan Keranjang");
        btnKosongkan.setMaxWidth(Double.MAX_VALUE);
        btnKosongkan.setOnAction(e -> {
            keranjang.kosongkanKeranjang();
            dashboard.updateCartBadge();
            dashboard.showKeranjangView();
        });

        summary.getChildren().addAll(titleSum, rowSub, rowDisc, rowPpn, rowPph, sep, rowTotal, btnCheckout, btnKosongkan);
        
        HBox layout = new HBox(20, scroll, summary);
        HBox.setHgrow(scroll, Priority.ALWAYS);
        summary.setPrefWidth(300); 
        layout.setAlignment(Pos.TOP_LEFT);
        
        content.getChildren().addAll(title, StyleKit.hSeparator(), layout);
        
        updateTotalSummary();

        return content;
    }

    private HBox buildItemRow(ItemKeranjang item) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10; -fx-border-color: #EEEEEE; -fx-border-radius: 10;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label lblNama = new Label(item.getProduk().getNama());
        lblNama.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 15px;");
        
        Label lblHarga = new Label(item.getProduk().getHargaFormatted() + " /pcs");
        lblHarga.setStyle("-fx-text-fill: gray; -fx-font-size: 13px;");
        
        info.getChildren().addAll(lblNama, lblHarga);

        Label lblSubtotal = new Label(item.getSubtotalFormatted());
        lblSubtotal.setStyle("-fx-text-fill: " + StyleKit.ACCENT + "; -fx-font-weight: bold; -fx-font-size: 15px;");

        Button btnMinus = new Button("−");
        btnMinus.setStyle("-fx-text-fill: black; -fx-background-color: #E0E0E0; -fx-cursor: hand; -fx-font-weight: bold;");
        
        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-text-fill: black; -fx-background-color: #E0E0E0; -fx-cursor: hand; -fx-font-weight: bold;");
        
        Label lblJumlah = new Label(String.valueOf(item.getJumlah()));
        lblJumlah.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px;");
        lblJumlah.setMinWidth(30);
        lblJumlah.setAlignment(Pos.CENTER);

        btnMinus.setOnAction(e -> {
            if (item.getJumlah() > 1) {
                item.setJumlah(item.getJumlah() - 1);
                lblJumlah.setText(String.valueOf(item.getJumlah()));
                lblSubtotal.setText(item.getSubtotalFormatted());
                updateTotalSummary(); 
            }
        });
        btnPlus.setOnAction(e -> {
            if (item.getJumlah() < item.getProduk().getStok()) {
                item.setJumlah(item.getJumlah() + 1);
                lblJumlah.setText(String.valueOf(item.getJumlah()));
                lblSubtotal.setText(item.getSubtotalFormatted());
                updateTotalSummary(); 
            }
        });

        HBox qtyBox = new HBox(8, btnMinus, lblJumlah, btnPlus);
        qtyBox.setAlignment(Pos.CENTER);
        
        Button btnHapus = new Button("✕");
        btnHapus.setStyle("-fx-background-color: transparent; -fx-text-fill: " + StyleKit.ACCENT + "; -fx-font-size: 16px; -fx-cursor: hand; -fx-font-weight: bold;");
        btnHapus.setOnAction(e -> {
            keranjang.hapusItem(item);
            dashboard.updateCartBadge();
            dashboard.showKeranjangView();
        });

        Region spacer1 = new Region(); spacer1.setMinWidth(20);
        Region spacer2 = new Region(); spacer2.setMinWidth(10);

        row.getChildren().addAll(info, qtyBox, spacer1, lblSubtotal, spacer2, btnHapus);
        return row;
    }

    private void updateTotalSummary() {
        if (lblSubtotalValue != null) {
            lblSubtotalValue.setText(keranjang.getSubtotalFormatted());
            
            // Diskon
            if (keranjang.getDiskon() > 0) {
                lblDiskonValue.setText(keranjang.getDiskonFormatted());
                rowDisc.setManaged(true);
                rowDisc.setVisible(true);
            } else {
                rowDisc.setManaged(false);
                rowDisc.setVisible(false);
            }

            lblPpnValue.setText(keranjang.getPpnFormatted());
            
            // PPh
            if (keranjang.getPph() > 0) {
                lblPphValue.setText(keranjang.getPphFormatted());
                rowPph.setManaged(true);
                rowPph.setVisible(true);
            } else {
                rowPph.setManaged(false);
                rowPph.setVisible(false);
            }
            
            totalValueLabel.setText(keranjang.getTotalFormatted());
        }
    }

    private void doCheckout() {
        showCustomConfirmationDialog(
            "Total Bayar: " + keranjang.getTotalFormatted(),
            "Yakin ingin melanjutkan pembayaran?",
            () -> {
                Transaksi trx = keranjang.checkout();
                if (trx != null) {
                    dashboard.updateCartBadge();
                    showCustomSuccessDialog("Pesanan " + trx.getRingkasan(), "Terima kasih telah berbelanja di ThreadHub!");
                    dashboard.showRiwayatView();
                } else {
                    // MENGGUNAKAN CUSTOM DIALOG UNTUK ERROR
                    showCustomErrorDialog("Checkout gagal. Periksa kembali stok produk yang Anda pesan.");
                }
            }
        );
    }

    private void showCustomConfirmationDialog(String headerText, String contentText, Runnable onConfirm) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1c29; -fx-background-radius: 15; -fx-border-color: #2d3142; -fx-border-radius: 15; -fx-border-width: 2;");

        Label icon = new Label("❓");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Konfirmasi Pembayaran");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");

        Label header = new Label(headerText);
        header.setStyle("-fx-text-fill: #a9b1d6; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label content = new Label(contentText);
        content.setStyle("-fx-text-fill: #8e95b3; -fx-font-size: 13px;");
        content.setTextAlignment(TextAlignment.CENTER);

        Button btnYes = StyleKit.primaryButton("Ya, Bayar");
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

    private void showCustomSuccessDialog(String headerText, String contentText) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        
        root.setStyle("-fx-background-color: #1a1c29; -fx-background-radius: 15; -fx-border-color: #2d3142; -fx-border-radius: 15; -fx-border-width: 2;");

        Label icon = new Label("🎉");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Pembayaran Berhasil!");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");

        Label header = new Label(headerText);
        header.setStyle("-fx-text-fill: #a9b1d6; -fx-font-size: 14px;");
        header.setTextAlignment(TextAlignment.CENTER);

        Label content = new Label(contentText);
        content.setStyle("-fx-text-fill: #8e95b3; -fx-font-size: 13px;");
        content.setTextAlignment(TextAlignment.CENTER);

        Button btnOk = StyleKit.primaryButton("OK");
        btnOk.setMinWidth(120);
        btnOk.setOnAction(e -> dialog.close());

        root.getChildren().addAll(icon, title, header, content, btnOk);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    private void showCustomErrorDialog(String msg) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1c29; -fx-background-radius: 15; -fx-border-color: #2d3142; -fx-border-radius: 15; -fx-border-width: 2;");

        Label icon = new Label("❌");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Checkout Gagal");
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
}