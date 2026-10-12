package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

    @Override
    @Transactional
    public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(
            @NotNull com.warehouse.api.beans.Warehouse data) {

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
                fromRequest(data);

        createWarehouseOperation.create(warehouse);

        return toWarehouseResponse(warehouse);
    }

    @Override
    public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
        Long numericId = parseId(id);

        Warehouse warehouse = warehouseRepository.findRecordById(numericId);

        if (warehouse == null) {
            throw new NotFoundException("Warehouse not found: " + id);
        }

        return toWarehouseResponse(warehouse);
    }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
      Long numericId = parseId(id);
      DbWarehouse record = warehouseRepository.findDbById(numericId);
      if (record == null || record.archivedAt != null) throw new NotFoundException("Active warehouse not found: " + id);
      Warehouse warehouse = record.toWarehouse();
      archiveWarehouseOperation.archive(warehouse);
  }

    @Transactional
    @Override
    public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(
            String businessUnitCode,
            @NotNull com.warehouse.api.beans.Warehouse data) {

        if (data == null) {
            throw new BadRequestException("Warehouse request body is required");
        }

        if (data.getBusinessUnitCode() != null
                && !businessUnitCode.equals(data.getBusinessUnitCode())) {
            throw new BadRequestException(
                    "Request businessUnitCode must match path businessUnitCode");
        }

        Warehouse warehouse = fromRequest(data);
        warehouse.businessUnitCode = businessUnitCode;

        replaceWarehouseOperation.replace(warehouse);

        return toWarehouseResponse(warehouse);
    }
    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (RuntimeException ex) {
            throw new BadRequestException("Warehouse id must be a numeric database id");
        }
    }

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
    fromRequest(com.warehouse.api.beans.Warehouse data) {

        if (data == null) {
            throw new BadRequestException("Warehouse request body is required");
        }

        var warehouse =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

        warehouse.businessUnitCode = data.getBusinessUnitCode();
        warehouse.location = data.getLocation();
        warehouse.capacity = data.getCapacity();
        warehouse.stock = data.getStock();

        return warehouse;
    }

    private com.warehouse.api.beans.Warehouse toWarehouseResponse(
            Warehouse warehouse) {

        com.warehouse.api.beans.Warehouse response =
                new com.warehouse.api.beans.Warehouse();

        response.setBusinessUnitCode(warehouse.businessUnitCode);
        response.setLocation(warehouse.location);
        response.setCapacity(warehouse.capacity);
        response.setStock(warehouse.stock);

        return response;
    }

}
