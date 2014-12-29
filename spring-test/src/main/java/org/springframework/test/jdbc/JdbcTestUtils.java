/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.jdbc;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.util.StringUtils;

/**
 * {@code JdbcTestUtils}은 JDBC 관련 유틸리티 함수들의 집합임.
 * 표준 db 테스팅 시나리오를 단순화 하는것을 목표로함.
 *
 * @author Thomas Risberg
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Chris Baldwin
 * @since 2.5.4
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.datasource.init.ScriptUtils
 * @see org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
 * @see org.springframework.jdbc.datasource.init.DatabasePopulatorUtils
 */
public class JdbcTestUtils {

	private static final Log logger = LogFactory.getLog(JdbcTestUtils.class);


	/**
	 * 주어진 테이블의 row count 반환.
	 * @param jdbcTemplate JDBC operation을 수행할 JdbcTemplate
	 * @param tableName 테이블 이름
	 * @return row count
	 */
	public static int countRowsInTable(JdbcTemplate jdbcTemplate, String tableName) {
		return jdbcTemplate.queryForObject("SELECT COUNT(0) FROM " + tableName, Integer.class);
	}

	/**
	 * 주어진 테이블의 {@code WHERE}조건의 row count 반환.
	 *
	 * <p> 만약 {@code WHERE}가 텍스트를 포함하면, {@code " WHERE "}를 앞에다 덧붙이고
	 * {@code SELECT} statement 뒤에 붙임.
	 * 예를 들어, 테이블 이름이 {@code "person"}이고 where 조건이 {@code "name = 'Bob' and age > 25"} 이면,
	 * 결과적인 SQL statement는 아래와 같음.
	 * {@code "SELECT COUNT(0) FROM person WHERE name = 'Bob' and age > 25"}.
     *
	 * @param jdbcTemplate JDBC operation을 수행할 JdbcTemplate
	 * @param tableName 테이블 이름
	 * @param whereClause {@code WHERE}절
	 * @return row count
	 */
	public static int countRowsInTableWhere(JdbcTemplate jdbcTemplate, String tableName, String whereClause) {
		String sql = "SELECT COUNT(0) FROM " + tableName;
		if (StringUtils.hasText(whereClause)) {
			sql += " WHERE " + whereClause;
		}
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}

	/**
	 * 명시된 테이블들 모든 row를 지움.
	 * @param jdbcTemplate JDBC operation을 수행할 JdbcTemplate
	 * @param tableName 테이블 이름
	 * @return  삭제된 총 row count
	 */
	public static int deleteFromTables(JdbcTemplate jdbcTemplate, String... tableNames) {
		int totalRowCount = 0;
		for (String tableName : tableNames) {
			int rowCount = jdbcTemplate.update("DELETE FROM " + tableName);
			totalRowCount += rowCount;
			if (logger.isInfoEnabled()) {
				logger.info("Deleted " + rowCount + " rows from table " + tableName);
			}
		}
		return totalRowCount;
	}

	/**
	 * 명시된 {@code WHERE}조건의 테이블의 모든 row를 지움.
	 *
	 * <p> 만약 {@code WHERE}가 텍스트를 포함하면, {@code " WHERE "}를 앞에다 덧붙이고
	 * {@code SELECT} statement 뒤에 붙임.
	 * 예를 들어, 테이블 이름이 {@code "person"}이고 where 조건이 {@code "name = 'Bob' and age > 25"} 이면,
	 * 결과적인 SQL statement는 아래와 같음.
	 * {@code "DELETE FROM person WHERE name = 'Bob' and age > 25"}.
	 *
	 * <p> {@code WHERE}절의 인자값들을 하드코딩하는것 대신에, {@code "?"}로 대체하여 인자를 따로 지정해주는것이 가능.
	 *
	 * @param jdbcTemplate JDBC operation을 수행할 JdbcTemplate
	 * @param tableName 테이블 이름
	 * @param whereClause {@code WHERE}절
	 * @param args where 조건에서 입력한 ?에 매핑되는 인자들 (PreparedStatement로 남겨두어 상응하는 SQL type을 추측케함);
	 * {@link SqlParameterValue} object를 포함하여 인자값 뿐만 아니라 SQL type과 scale(option)을 명시할 가능성이 있음.
	 * @return  삭제된 총 row count
	 */
	public static int deleteFromTableWhere(JdbcTemplate jdbcTemplate, String tableName, String whereClause,
			Object... args) {
		String sql = "DELETE FROM " + tableName;
		if (StringUtils.hasText(whereClause)) {
			sql += " WHERE " + whereClause;
		}
		int rowCount = (args != null && args.length > 0 ? jdbcTemplate.update(sql, args) : jdbcTemplate.update(sql));
		if (logger.isInfoEnabled()) {
			logger.info("Deleted " + rowCount + " rows from table " + tableName);
		}
		return rowCount;
	}

	/**
	 * 명시된 테이블 drop.
	 * @param jdbcTemplate JDBC operation을 수행할 JdbcTemplate
	 * @param tableName 테이블 이름
	 */
	public static void dropTables(JdbcTemplate jdbcTemplate, String... tableNames) {
		for (String tableName : tableNames) {
			jdbcTemplate.execute("DROP TABLE " + tableName);
			if (logger.isInfoEnabled()) {
				logger.info("Dropped table " + tableName);
			}
		}
	}

	/**
	 * Execute the given SQL script.
	 * <p>The script will typically be loaded from the classpath. There should
	 * be one statement per line. Any semicolons and line comments will be removed.
	 * <p><b>Do not use this method to execute DDL if you expect rollback.</b>
	 * @param jdbcTemplate the JdbcTemplate with which to perform JDBC operations
	 * @param resourceLoader the resource loader with which to load the SQL script
	 * @param sqlResourcePath the Spring resource path for the SQL script
	 * @param continueOnError whether or not to continue without throwing an
	 * exception in the event of an error
	 * @throws DataAccessException if there is an error executing a statement
	 * and {@code continueOnError} is {@code false}
	 * @see ResourceDatabasePopulator
	 * @see #executeSqlScript(JdbcTemplate, Resource, boolean)
	 * @deprecated as of Spring 4.0.3, in favor of using
	 * {@link org.springframework.jdbc.datasource.init.ScriptUtils#executeSqlScript}
	 * or {@link org.springframework.jdbc.datasource.init.ResourceDatabasePopulator}.
	 */
	@Deprecated
	public static void executeSqlScript(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader,
			String sqlResourcePath, boolean continueOnError) throws DataAccessException {
		Resource resource = resourceLoader.getResource(sqlResourcePath);
		executeSqlScript(jdbcTemplate, resource, continueOnError);
	}

	/**
	 * Execute the given SQL script.
	 * <p>The script will typically be loaded from the classpath. Statements
	 * should be delimited with a semicolon. If statements are not delimited with
	 * a semicolon then there should be one statement per line. Statements are
	 * allowed to span lines only if they are delimited with a semicolon. Any
	 * line comments will be removed.
	 * <p><b>Do not use this method to execute DDL if you expect rollback.</b>
	 * @param jdbcTemplate the JdbcTemplate with which to perform JDBC operations
	 * @param resource the resource to load the SQL script from
	 * @param continueOnError whether or not to continue without throwing an
	 * exception in the event of an error
	 * @throws DataAccessException if there is an error executing a statement
	 * and {@code continueOnError} is {@code false}
	 * @see ResourceDatabasePopulator
	 * @see #executeSqlScript(JdbcTemplate, EncodedResource, boolean)
	 * @deprecated as of Spring 4.0.3, in favor of using
	 * {@link org.springframework.jdbc.datasource.init.ScriptUtils#executeSqlScript}
	 * or {@link org.springframework.jdbc.datasource.init.ResourceDatabasePopulator}.
	 */
	@Deprecated
	public static void executeSqlScript(JdbcTemplate jdbcTemplate, Resource resource, boolean continueOnError)
			throws DataAccessException {
		executeSqlScript(jdbcTemplate, new EncodedResource(resource), continueOnError);
	}

	/**
	 * Execute the given SQL script.
	 * <p>The script will typically be loaded from the classpath. There should
	 * be one statement per line. Any semicolons and line comments will be removed.
	 * <p><b>Do not use this method to execute DDL if you expect rollback.</b>
	 * @param jdbcTemplate the JdbcTemplate with which to perform JDBC operations
	 * @param resource the resource (potentially associated with a specific encoding)
	 * to load the SQL script from
	 * @param continueOnError whether or not to continue without throwing an
	 * exception in the event of an error
	 * @throws DataAccessException if there is an error executing a statement
	 * and {@code continueOnError} is {@code false}
	 * @see ResourceDatabasePopulator
	 * @deprecated as of Spring 4.0.3, in favor of using
	 * {@link org.springframework.jdbc.datasource.init.ScriptUtils#executeSqlScript}
	 * or {@link org.springframework.jdbc.datasource.init.ResourceDatabasePopulator}.
	 */
	@Deprecated
	public static void executeSqlScript(JdbcTemplate jdbcTemplate, EncodedResource resource, boolean continueOnError)
			throws DataAccessException {
		new ResourceDatabasePopulator(continueOnError, false, resource.getEncoding(), resource.getResource()).execute(jdbcTemplate.getDataSource());
	}

	/**
	 * Read a script from the provided {@code LineNumberReader}, using
	 * "{@code --}" as the comment prefix, and build a {@code String} containing
	 * the lines.
	 * @param lineNumberReader the {@code LineNumberReader} containing the script
	 * to be processed
	 * @return a {@code String} containing the script lines
	 * @see #readScript(LineNumberReader, String)
	 * @deprecated as of Spring 4.0.3, in favor of using
	 * {@link org.springframework.jdbc.datasource.init.ScriptUtils#readScript(LineNumberReader, String, String)}
	 */
	@Deprecated
	public static String readScript(LineNumberReader lineNumberReader) throws IOException {
		return readScript(lineNumberReader, ScriptUtils.DEFAULT_COMMENT_PREFIX);
	}

	/**
	 * Read a script from the provided {@code LineNumberReader}, using the supplied
	 * comment prefix, and build a {@code String} containing the lines.
	 * <p>Lines <em>beginning</em> with the comment prefix are excluded from the
	 * results; however, line comments anywhere else &mdash; for example, within
	 * a statement &mdash; will be included in the results.
	 * @param lineNumberReader the {@code LineNumberReader} containing the script
	 * to be processed
	 * @param commentPrefix the prefix that identifies comments in the SQL script &mdash; typically "--"
	 * @return a {@code String} containing the script lines
	 * @deprecated as of Spring 4.0.3, in favor of using
	 * {@link org.springframework.jdbc.datasource.init.ScriptUtils#readScript(LineNumberReader, String, String)}
	 */
	@Deprecated
	public static String readScript(LineNumberReader lineNumberReader, String commentPrefix) throws IOException {
		return ScriptUtils.readScript(lineNumberReader, commentPrefix, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR);
	}

	/**
	 * Determine if the provided SQL script contains the specified delimiter.
	 * @param script the SQL script
	 * @param delim character delimiting each statement &mdash; typically a ';' character
	 * @return {@code true} if the script contains the delimiter; {@code false} otherwise
	 * @deprecated as of Spring 4.0.3, in favor of using
	 * {@link org.springframework.jdbc.datasource.init.ScriptUtils#containsSqlScriptDelimiters}
	 */
	@Deprecated
	public static boolean containsSqlScriptDelimiters(String script, char delim) {
		return ScriptUtils.containsSqlScriptDelimiters(script, String.valueOf(delim));
	}

	/**
	 * Split an SQL script into separate statements delimited by the provided
	 * delimiter character. Each individual statement will be added to the
	 * provided {@code List}.
	 * <p>Within a statement, "{@code --}" will be used as the comment prefix;
	 * any text beginning with the comment prefix and extending to the end of
	 * the line will be omitted from the statement. In addition, multiple adjacent
	 * whitespace characters will be collapsed into a single space.
	 * @param script the SQL script
	 * @param delim character delimiting each statement &mdash; typically a ';' character
	 * @param statements the list that will contain the individual statements
	 * @deprecated as of Spring 4.0.3, in favor of using
	 * {@link org.springframework.jdbc.datasource.init.ScriptUtils#splitSqlScript(String, char, List)}
	 */
	@Deprecated
	public static void splitSqlScript(String script, char delim, List<String> statements) {
		ScriptUtils.splitSqlScript(script, delim, statements);
	}
}
