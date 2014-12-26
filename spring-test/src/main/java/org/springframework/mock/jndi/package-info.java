/**
 * 왠만하면 동작하는 JNDI SPI의 가장 단순한 구현.
 *
 * <p>test suite를 위해서나 stand-alone application에서 단순한 JNDI environment를 준비하는데 유용함.
 * 예를 들어, JDBC DataSource들이 같은 JNDI 이름으로 Java EE container에 설정되어 있다면,
 * 어플리케이션 코드와 설정이 변경없이 재활용 가능함.
 * </p>
 *
 */
package org.springframework.mock.jndi;
