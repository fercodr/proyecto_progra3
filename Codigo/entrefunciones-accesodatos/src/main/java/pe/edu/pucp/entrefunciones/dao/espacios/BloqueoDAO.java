package pe.edu.pucp.entrefunciones.dao.espacios;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface BloqueoDAO extends DAO<Bloqueo, Integer> {
    List<Bloqueo> findBySalaId(int salaId) throws SQLException;
    List<Bloqueo> findByRecursoId(int recursoId) throws SQLException;
    boolean existsOverlapBySalaId(int salaId, LocalDateTime fechaInicio,
                                  LocalDateTime fechaFin, Integer idExcluir) throws SQLException;
    boolean existsOverlapByRecursoId(int recursoId, LocalDateTime fechaInicio,
                                     LocalDateTime fechaFin, Integer idExcluir) throws SQLException;
}
