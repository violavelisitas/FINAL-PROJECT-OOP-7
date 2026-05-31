package ThreadHub.controller;

import ThreadHub.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
  private static DataStore instance;
  private final List<User> users = new ArrayList<>();
  private final List<Produk> produkList = new ArrayList<>();
  private final List<Transaksi> transaksiList = new ArrayList<>();
  private final List<OutfitBundle> outfitList = new ArrayList<>();
  private static final String FILE_PRODUK = "data_produk.dat";
  private static final String FILE_TRANSAKSI = "data_transaksi.dat";
  private static final String FILE_USER = "data_user.dat";
  private static final String FILE_OUTFIT = "data_outfit.dat";

  private DataStore() {
    muatDataUser();
    muatDataProduk();
    muatDataTransaksi();
    muatDataOutfit();
  }

  public static DataStore getInstance() {
    if (instance == null) {
      instance = new DataStore();
    }
    return instance;
  }

  @SuppressWarnings("unchecked")
  private void muatDataUser() {
    File file = new File(FILE_USER);
    if (file.exists()) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        List<User> dataTersimpan = (List<User>) ois.readObject();
        users.addAll(dataTersimpan);
      } catch (Exception e) {
        initUsers();
        simpanDataUser();
      }
    } else {
      initUsers();
      simpanDataUser();
    }
  }

  private void initUsers() {
    users.add(new Admin(1, "admin", "admin123", "Administrator"));
  }

  public User login(String username, String password) {
    return users.stream()
      .filter(u -> u.getUsername().equals(username) && u.cekPassword(password))
      .findFirst()
      .orElse(null);
  }

  public List<User> getAllUsers() { return users; }

  public void simpanDataUser() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_USER))) {
      oos.writeObject(users);
    } catch (Exception e) {
      System.out.println("Gagal menyimpan data user: " + e.getMessage());
    }
  }

  public void tambahUser(User u) {
    users.add(u);
    simpanDataUser();
  }

  public void hapusUser(int id) {
    users.removeIf(u -> u.getId() == id);
    simpanDataUser();

    boolean isTransaksiDihapus = transaksiList.removeIf(t -> t.getBuyer().getId() == id);
    if (isTransaksiDihapus) {
      simpanDataTransaksi();
    }
  }

  public int generateUserId() {
    return users.stream().mapToInt(User::getId).max().orElse(0) + 1;
  }

  @SuppressWarnings("unchecked")
  private void muatDataProduk() {
    File file = new File(FILE_PRODUK);
    if (file.exists()) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        List<Produk> dataTersimpan = (List<Produk>) ois.readObject();
        produkList.addAll(dataTersimpan);
      } catch (Exception e) {
        initSampleProduk();
        simpanDataProduk();
      }
    } else {
      initSampleProduk();
      simpanDataProduk();
    }
  }

  public void simpanDataProduk() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PRODUK))) {
      oos.writeObject(produkList);
    } catch (Exception e) {
      System.out.println("Gagal menyimpan data produk: " + e.getMessage());
    }
  }

  private void initSampleProduk() {
    produkList.add(new Produk(1, "Kaos Polos Premium", "Kaos bahan cotton combed 30s", "Kaos", 89_000, 50, "M", "Putih", "PRIA", null));
    produkList.add(new Produk(2, "Kemeja Flanel Kotak", "Kemeja casual", "Kemeja", 175_000, 30, "L", "Merah", "PRIA", null));
    produkList.add(new Produk(3, "Blouse Floral Elegan", "Blouse cantik", "Kemeja", 150_000, 25, "M", "Navy", "WANITA", null));
    produkList.add(new Produk(4, "Rok Midi Plisket", "Rok premium", "Rok", 120_000, 15, "All Size", "Hitam", "WANITA", null));
    produkList.add(new Produk(5, "Kaos Karakter Superhero", "Kaos anak", "Kaos", 65_000, 20, "S", "Biru", "ANAK-ANAK", null));
    produkList.add(new Produk(6, "Polo Shirt Pique", "Polo formal", "Kaos Polo", 145_000, 40, "M", "Dongker", "PRIA", null));
    produkList.add(new Produk(7, "Sepatu Sneakers Canvas", "Sepatu kasual", "Sepatu", 285_000, 18, "40", "Putih", "WANITA", null));
    produkList.add(new Produk(8, "Topi Baseball Anak", "Topi pelindung", "Topi", 45_000, 22, "All Size", "Merah", "ANAK-ANAK", null));
  }

  public List<Produk> getAllProduk() { return produkList; }

  public List<Produk> cariProduk(String keyword) {
    String kw = keyword.toLowerCase();
    return produkList.stream()
      .filter(p -> p.getNama().toLowerCase().contains(kw)
                || p.getKategori().toLowerCase().contains(kw)
                || p.getWarna().toLowerCase().contains(kw)
                || p.getGender().toLowerCase().contains(kw))
      .toList();
  }

  public Produk getProdukById(int id) {
    return produkList.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
  }

  public void tambahProduk(Produk p) {
    produkList.add(p);
    simpanDataProduk();
  }

  public void hapusProduk(int id) {
    produkList.removeIf(p -> p.getId() == id);
    simpanDataProduk();
  }

  public int generateProdukId() {
    return produkList.stream().mapToInt(Produk::getId).max().orElse(0) + 1;
  }

  @SuppressWarnings("unchecked")
  private void muatDataTransaksi() {
    File file = new File(FILE_TRANSAKSI);
    if (file.exists()) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        List<Transaksi> dataTersimpan = (List<Transaksi>) ois.readObject();
        transaksiList.addAll(dataTersimpan);
      } catch (Exception e) {
        System.out.println("Gagal memuat data transaksi.");
      }
    }
  }

  public void simpanDataTransaksi() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_TRANSAKSI))) {
      oos.writeObject(transaksiList);
    } catch (Exception e) {
      System.out.println("Gagal menyimpan data transaksi: " + e.getMessage());
    }
  }

  public void tambahTransaksi(Transaksi t) {
    transaksiList.add(t);
    simpanDataTransaksi();
  }

  public int generateTransaksiId() {
    return transaksiList.stream().mapToInt(Transaksi::getId).max().orElse(0) + 1;
  }

  public List<Transaksi> getTransaksiBuyer(Buyer buyer) {
    return transaksiList.stream()
      .filter(t -> t.getBuyer().getId() == buyer.getId())
      .toList();
  }

  public List<Transaksi> getAllTransaksi() { return transaksiList; }

  @SuppressWarnings("unchecked")
  private void muatDataOutfit() {
    File file = new File(FILE_OUTFIT);
    if (file.exists()) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        List<OutfitBundle> dataTersimpan = (List<OutfitBundle>) ois.readObject();
        outfitList.addAll(dataTersimpan);
      } catch (Exception e) {
        initSampleOutfit();
      }
    } else {
      initSampleOutfit();
    }
  }

  public void simpanDataOutfit() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_OUTFIT))) {
      oos.writeObject(outfitList);
    } catch (Exception e) {
      System.out.println("Gagal menyimpan data outfit: " + e.getMessage());
    }
  }

  private void initSampleOutfit() {
    OutfitBundle streetwear = new OutfitBundle(1, "Streetwear Starter Pack", "STREETWEAR", "Kombinasi kasual jalanan yang modis", null);
    streetwear.tambahProdukKePaket(getProdukById(1));
    streetwear.tambahProdukKePaket(getProdukById(2));

    OutfitBundle casualWoman = new OutfitBundle(2, "Summer Casual Chic", "CASUAL", "Gaya santai nan elegan untuk wanita", null);
    casualWoman.tambahProdukKePaket(getProdukById(3));
    casualWoman.tambahProdukKePaket(getProdukById(7));

    outfitList.add(streetwear);
    outfitList.add(casualWoman);
    simpanDataOutfit();
  }

  public List<OutfitBundle> getAllOutfit() { return outfitList; }

  public List<OutfitBundle> getOutfitByStyle(String style) {
    return outfitList.stream()
      .filter(o -> o.getKategoriStyle().equalsIgnoreCase(style))
      .toList();
  }
}