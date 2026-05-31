package ThreadHub.buyer;

import ThreadHub.controller.KeranjangController;
import ThreadHub.model.*;
import ThreadHub.view.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

public class KeranjangView {
  private final BuyerDashboardView dashboard;
  private final KeranjangController keranjang;
  private Label totalValueLabel;

  public KeranjangView(BuyerDashboardView dashboard, KeranjangController keranjang) {
    this.dashboard = dashboard;
    this.keranjang = keranjang;
  }

  public VBox build() {
    VBox content = new VBox(20);
    content.setPadding(new Insets(30));
    content.setStyle("-fx-background-color: #FFFFFF;");

    Label title = StyleKit.titleLabel("Keranjang Belanja", 22);
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

    VBox summary = StyleKit.card(20);
    summary.setSpacing(12);

    Label totalLabel = new Label("Total Pembayaran");
    totalLabel.setStyle("-fx-text-fill: #eaeaea; -fx-font-size: 14px;");

    totalValueLabel = new Label(keranjang.getTotalFormatted());
    totalValueLabel.setStyle("-fx-text-fill: " + StyleKit.ACCENT + "; -fx-font-weight: bold; -fx-font-size: 28px;");

    Button btnCheckout = StyleKit.primaryButton("Bayar Sekarang");
    btnCheckout.setMaxWidth(Double.MAX_VALUE);
    btnCheckout.setOnAction(e -> doCheckout());

    Button btnKosongkan = StyleKit.outlineButton("Kosongkan Keranjang");
    btnKosongkan.setMaxWidth(Double.MAX_VALUE);
    btnKosongkan.setOnAction(e -> {
      keranjang.kosongkanKeranjang();
      dashboard.updateCartBadge();
      dashboard.showKeranjangView();
    });

    summary.getChildren().addAll(totalLabel, totalValueLabel, StyleKit.hSeparator(), btnCheckout, btnKosongkan);

    HBox layout = new HBox(20, scroll, summary);
    HBox.setHgrow(scroll, Priority.ALWAYS);
    summary.setPrefWidth(280);
    layout.setAlignment(Pos.TOP_LEFT);

    content.getChildren().addAll(title, StyleKit.hSeparator(), layout);
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

    Button btnMinus = new Button("-");
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

    Button btnHapus = new Button("x");
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
    if (totalValueLabel != null) {
      totalValueLabel.setText(keranjang.getTotalFormatted());
    }
  }

  private void doCheckout() {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Konfirmasi Pembayaran");
    confirm.setHeaderText("Total: " + keranjang.getTotalFormatted());
    confirm.setContentText("Yakin ingin melanjutkan pembayaran?");
    confirm.showAndWait().ifPresent(btn -> {
      if (btn == ButtonType.OK) {
        Transaksi trx = keranjang.checkout();
        if (trx != null) {
          dashboard.updateCartBadge();
          Alert sukses = new Alert(Alert.AlertType.INFORMATION);
          sukses.setTitle("Pembayaran Berhasil!");
          sukses.setHeaderText("Pesanan " + trx.getRingkasan());
          sukses.setContentText("Terima kasih telah berbelanja di ThreadHub!");
          sukses.showAndWait();
          dashboard.showRiwayatView();
        } else {
          new Alert(Alert.AlertType.ERROR, "Checkout gagal. Periksa stok produk.").showAndWait();
        }
      }
    });
  }
}