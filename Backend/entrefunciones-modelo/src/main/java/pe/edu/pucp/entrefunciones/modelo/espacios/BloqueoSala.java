package pe.edu.pucp.entrefunciones.modelo.espacios;

import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;

public class BloqueoSala implements IEliminable {
    private int id;
    private Sala sala;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private boolean activo = true;

    public BloqueoSala() {
    }

    public BloqueoSala(int id, Sala sala, LocalDateTime inicio, LocalDateTime fin) {
        setId(id);
        setSala(sala);
        setInicio(inicio);
        setFin(fin);
    }

    public int getId() { return id; }
    public void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("El id no puede ser negativo");
        this.id = id;
    }
    public Sala getSala() { return sala; }
    public void setSala(Sala sala) {
        if (sala == null) throw new IllegalArgumentException("La sala es obligatoria");
        this.sala = sala;
    }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) {
        if (inicio == null) throw new IllegalArgumentException("El inicio es obligatorio");
        this.inicio = inicio;
    }
    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) {
        if (fin == null) throw new IllegalArgumentException("El fin es obligatorio");
        this.fin = fin;
    }
    @Override public void activar() { activo = true; }
    @Override public void desactivar() { activo = false; }
    @Override public boolean estaActivo() { return activo; }
}
