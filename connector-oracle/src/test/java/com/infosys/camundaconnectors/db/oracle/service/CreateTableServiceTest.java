/*
 * Copyright (c) 2022 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.db.oracle.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.infosys.camundaconnectors.db.oracle.model.response.OracleDBResponse;
import com.infosys.camundaconnectors.db.oracle.model.response.QueryResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateTableServiceTest {
  @Mock private Connection connectionMock;
  @Mock private Statement statementMock;
  private CreateTableService service;

  @BeforeEach
  public void init() {
    service = new CreateTableService();
    service.setDatabaseName("XE");
    service.setTableName("testRio");
    service.setColumnsList(
        List.of(
            Map.of(
                "colName",
                "personid",
                "DataType",
                "int",
                "Constraint",
                new ArrayList<>(List.of("NOT NULL", "PRIMARY KEY"))),
            Map.of("colName", "lastname", "DataType", "varchar2(50)"),
            Map.of("colName", "firstname", "DataType", "varchar2(50)", "Constraint", "NOT NULL"),
            Map.of("colName", "address", "DataType", "varchar2(50)"),
            Map.of("colName", "city", "DataType", "varchar2(50)")));
  }

  @DisplayName("Should create table")
  @Test
  void shouldCreateTableWhenExecute() throws SQLException {
    // given
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(statementMock.execute(anyString())).thenReturn(true);
    // When
    OracleDBResponse result = service.invoke(connectionMock);
    // Then
    assertThat(result).isInstanceOf(QueryResponse.class);
    @SuppressWarnings("unchecked")
    QueryResponse<String> queryResponse = (QueryResponse<String>) result;
    Mockito.verify(statementMock, Mockito.times(1)).execute(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
    assertThat(queryResponse)
        .extracting("response")
        .asInstanceOf(STRING)
        .contains("Table '" + service.getTableName() + "' created successfully");
  }

  @DisplayName("Should throw error as columnsList is not set")
  @Test
  void shouldThrowErrorEmptyColumnsList() throws SQLException {
    // given
    service.setColumnsList(List.of());
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(statementMock.execute(anyString()))
        .thenThrow(
            new SQLException(
                "Invalid 'columnsList', It should be a list of maps for column, "
                    + "with keys: 'colName', 'dataType' and optional 'constraints'"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "Invalid 'columnsList', It should be a list of maps for column, "
                + "with keys: 'colName', 'dataType' and optional 'constraints'");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as columnsList is invalid")
  @Test
  void shouldThrowErrorInvalidColumnsList() throws SQLException {
    // given
    service.setColumnsList(List.of(Map.of("name", "op")));
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(statementMock.execute(anyString()))
        .thenThrow(new SQLException("colName or dataType can't be null or empty"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("colName or dataType can't be null or empty");
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }

  @DisplayName("Should throw error as table name is invalid")
  @Test
  void shouldThrowErrorInvalidTable() throws SQLException {
    // given
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(statementMock.execute(anyString()))
        .thenThrow(new SQLException("Table 'trialtable7' already exists"));
    // when
    assertThatThrownBy(() -> service.invoke(connectionMock))
        // then
        .isInstanceOf(SQLException.class)
        .hasMessageContaining("Table 'trialtable7' already exists");
    Mockito.verify(statementMock, Mockito.times(1)).execute(any(String.class));
    Mockito.verify(connectionMock, Mockito.times(1)).close();
  }
}
