package com.byoutline.mockserver.internal

import com.byoutline.mockserver.DefaultValues
import org.simpleframework.transport.Server
import org.simpleframework.transport.connect.Connection
import spock.lang.Specification
import spock.lang.Unroll

class AutoPortConnectSpec extends Specification {
    Server server = Mock()
    def connector = new MockConnector()
    def instance = new AutoPortConnect(connector)

    @Unroll
    def "should attempt to connect to ports: #expPorts for minPort: #minPort, maxPort: #maxPort given unavailable ports: #errorPorts"() {
        given: 'Mock connector'
        connector.errorPorts = errorPorts

        when: 'ports are passed to method'
        instance.connectToPortFromRange(server, minPort, maxPort)

        then: 'expected ports were attempted'
        connector.attemptedPorts == expPorts

        where:
        minPort | maxPort | errorPorts           | expPorts
        1       | 1       | []                   | [1]
        2       | 4       | [2, 3]               | [2, 3, 4]
        10      | 15      | []                   | [10]
        10      | 15      | [10, 11, 12, 13, 14] | [10, 11, 12, 13, 14, 15]
    }

    @Unroll
    def "should throw exception if all ports were busy #ports"() {
        given:
        def minPort = ports[0]
        def maxPort = ports[1]
        connector.errorPorts = minPort..maxPort + 1

        when:
        instance.connectToPortFromRange(server, minPort, maxPort)

        then:
        thrown IOException

        where:
        ports << [[DefaultValues.MOCK_SERVER_PORT, DefaultValues.MOCK_SERVER_PORT], [1, 5]]
    }

    @Unroll
    def "should throw exception if invalid ports were passed: #ports"() {
        given:
        def minPort = ports[0]
        def maxPort = ports[1]
        connector.errorPorts = minPort..maxPort + 1

        when:
        instance.connectToPortFromRange(server, minPort, maxPort)

        then:
        thrown IllegalArgumentException

        where:
        ports << [[-1, 1], [70000, 70000], [2, 1]]
    }

    def "should try only single port if port specified and throw exception if it fails"() {
        given:
        connector.errorPorts = [1]

        when:
        instance.connectToPortFromRange(server, 1, 1)

        then:
        connector.attemptedPorts == [1]
        thrown IOException
    }
}

class MockConnector implements Connector {
    List<Integer> attemptedPorts = []
    List<Integer> errorPorts = []

    @Override
    Connection connectToPort(int port, Server server) throws IOException {
        attemptedPorts.add(port)
        if (errorPorts.contains(port)) {
            throw new IOException("Port busy")
        }
        return null
    }
}