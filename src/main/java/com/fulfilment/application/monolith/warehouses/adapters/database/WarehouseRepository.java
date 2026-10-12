package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
      DbWarehouse db = fromWarehouse(warehouse);
      persist(db);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
      DbWarehouse db = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
      if (db == null) throw new jakarta.ws.rs.NotFoundException("Active warehouse " + warehouse.businessUnitCode + " does not exist");
      db.location = warehouse.location;
      db.capacity = warehouse.capacity;
      db.stock = warehouse.stock;
      db.createdAt = warehouse.createdAt;
      db.archivedAt = warehouse.archivedAt;
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
      DbWarehouse db = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
      if (db != null) delete(db);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
      DbWarehouse db = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
      return db == null ? null : db.toWarehouse();
  }

    public DbWarehouse findDbById(Long id) {
        return PanacheRepository.super.findById(id);
    }

    public Warehouse findRecordById(Long id) {
        DbWarehouse db = findDbById(id);
        return db == null ? null : db.toWarehouse();
    }

    public List<Warehouse> getActiveAtLocation(String location) {
        return find("location = ?1 and archivedAt is null", location).list().stream().map(DbWarehouse::toWarehouse).toList();
    }

    public List<Warehouse> getActive() {
        return find("archivedAt is null").list().stream().map(DbWarehouse::toWarehouse).toList();
    }

    public void archiveById(Long id, java.time.LocalDateTime archivedAt) {
        DbWarehouse db = findDbById(id);
        if (db != null) db.archivedAt = archivedAt;
    }

    private DbWarehouse fromWarehouse(Warehouse warehouse) {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = warehouse.businessUnitCode;
        db.location = warehouse.location;
        db.capacity = warehouse.capacity;
        db.stock = warehouse.stock;
        db.createdAt = warehouse.createdAt;
        db.archivedAt = warehouse.archivedAt;
        return db;
    }
}
