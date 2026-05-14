package com.upsjb.ms3.domain.value;

import java.io.Serializable;
import java.util.Objects;

public record StockValue(int stockFisico, int stockReservado) implements Serializable {

    public StockValue {
        if (stockFisico < 0) {
            throw new IllegalArgumentException("El stock físico no puede ser negativo.");
        }

        if (stockReservado < 0) {
            throw new IllegalArgumentException("El stock reservado no puede ser negativo.");
        }

        if (stockReservado > stockFisico) {
            throw new IllegalArgumentException("El stock reservado no puede superar el stock físico.");
        }
    }

    public static StockValue of(int stockFisico, int stockReservado) {
        return new StockValue(stockFisico, stockReservado);
    }

    public static StockValue zero() {
        return new StockValue(0, 0);
    }

    public int stockDisponible() {
        return stockFisico - stockReservado;
    }

    public boolean hasDisponible(int cantidad) {
        validarCantidadPositiva(cantidad);
        return stockDisponible() >= cantidad;
    }

    public StockValue entrada(int cantidad) {
        validarCantidadPositiva(cantidad);
        return new StockValue(stockFisico + cantidad, stockReservado);
    }

    public StockValue salidaDisponible(int cantidad) {
        validarCantidadPositiva(cantidad);

        if (cantidad > stockDisponible()) {
            throw new IllegalArgumentException("Stock disponible insuficiente.");
        }

        return new StockValue(stockFisico - cantidad, stockReservado);
    }

    public StockValue reservar(int cantidad) {
        validarCantidadPositiva(cantidad);

        if (cantidad > stockDisponible()) {
            throw new IllegalArgumentException("Stock disponible insuficiente para reservar.");
        }

        return new StockValue(stockFisico, stockReservado + cantidad);
    }

    public StockValue liberarReserva(int cantidad) {
        validarCantidadPositiva(cantidad);

        if (cantidad > stockReservado) {
            throw new IllegalArgumentException("La cantidad a liberar supera el stock reservado.");
        }

        return new StockValue(stockFisico, stockReservado - cantidad);
    }

    public StockValue confirmarReserva(int cantidad) {
        validarCantidadPositiva(cantidad);

        if (cantidad > stockReservado) {
            throw new IllegalArgumentException("La cantidad a confirmar supera el stock reservado.");
        }

        return new StockValue(stockFisico - cantidad, stockReservado - cantidad);
    }

    public boolean isBajoStock(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }

        return stockDisponible() <= stockMinimo;
    }

    private static void validarCantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof StockValue that)) {
            return false;
        }

        return stockFisico == that.stockFisico && stockReservado == that.stockReservado;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockFisico, stockReservado);
    }

    @Override
    public String toString() {
        return "StockValue{stockFisico=" + stockFisico
                + ", stockReservado=" + stockReservado
                + ", stockDisponible=" + stockDisponible()
                + '}';
    }
}