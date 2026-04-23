package cl.apipedidos.cliente.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClienteSchemaMigrator implements CommandLineRunner {

    private final DataSource dataSource;

    public ClienteSchemaMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Set<String> columnasExistentes = obtenerColumnasDeClientes(connection);
                asegurarColumnasObligatorias(connection, columnasExistentes);
                limpiarYReordenarClientes(connection);
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void asegurarColumnasObligatorias(Connection connection, Set<String> columnasExistentes) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (!columnasExistentes.contains("EMAIL_CL")) {
                statement.executeUpdate("ALTER TABLE clientes ADD email_cl VARCHAR(254) NOT NULL DEFAULT ''");
            }

            if (!columnasExistentes.contains("TELEFONO_CL")) {
                statement.executeUpdate("ALTER TABLE clientes ADD telefono_cl VARCHAR(30) NOT NULL DEFAULT ''");
            }
        }
    }

    private void limpiarYReordenarClientes(Connection connection) throws SQLException {
        eliminarFilasIncompletas(connection);
        renumerarClientesSecuencialmente(connection);
    }

    private void eliminarFilasIncompletas(Connection connection) throws SQLException {
        String deleteSql = """
            delete from clientes
            where coalesce(trim(nombre_cl), '') = ''
               or rut_cl is null
               or coalesce(trim(div_cl), '') = ''
               or coalesce(trim(direccion_cl), '') = ''
               or coalesce(trim(email_cl), '') = ''
               or coalesce(trim(telefono_cl), '') = ''
               or id_comuna is null
               or fecha_registro is null
            """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteSql);
        }
    }

    private void renumerarClientesSecuencialmente(Connection connection) throws SQLException {
        List<Long> idsOrdenados = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("select id_cliente from clientes order by id_cliente asc")) {
            while (resultSet.next()) {
                idsOrdenados.add(resultSet.getLong("id_cliente"));
            }
        }

        if (idsOrdenados.isEmpty()) {
            return;
        }

        long maxIdActual = idsOrdenados.get(idsOrdenados.size() - 1);
        long offsetTemporal = maxIdActual + idsOrdenados.size() + 1000L;

        try (PreparedStatement updateTemporal = connection.prepareStatement("update clientes set id_cliente = ? where id_cliente = ?")) {
            for (int index = 0; index < idsOrdenados.size(); index++) {
                long idActual = idsOrdenados.get(index);
                long idTemporal = offsetTemporal + index + 1;
                updateTemporal.setLong(1, idTemporal);
                updateTemporal.setLong(2, idActual);
                updateTemporal.addBatch();
            }
            updateTemporal.executeBatch();
        }

        try (PreparedStatement updateFinal = connection.prepareStatement("update clientes set id_cliente = ? where id_cliente = ?")) {
            for (int index = 0; index < idsOrdenados.size(); index++) {
                long idTemporal = offsetTemporal + index + 1;
                long idFinal = index + 1L;
                updateFinal.setLong(1, idFinal);
                updateFinal.setLong(2, idTemporal);
                updateFinal.addBatch();
            }
            updateFinal.executeBatch();
        }
    }

    private Set<String> obtenerColumnasDeClientes(Connection connection) throws SQLException {
        Set<String> columnas = new HashSet<>();

        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, "CLIENTES", null)) {
            while (resultSet.next()) {
                columnas.add(resultSet.getString("COLUMN_NAME").toUpperCase());
            }
        }

        if (columnas.isEmpty()) {
            try (ResultSet resultSet = metaData.getColumns(null, null, "clientes", null)) {
                while (resultSet.next()) {
                    columnas.add(resultSet.getString("COLUMN_NAME").toUpperCase());
                }
            }
        }

        return columnas;
    }
}