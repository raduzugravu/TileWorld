package com.tileworld.helper

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class DistanceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test findShortestPath"() {

        given:
        def map = [
            ["H","E","E","A"],
            ["E","O","H","T"],
            ["T","O","O","T"],
            ["A","E","E","E"]
        ]

        when:
        def distance = Distance.findShortestPath([3,0],[1,2], map)

        then:
        "east".equalsIgnoreCase(distance[0]);
        "east".equalsIgnoreCase(distance[1]);
        "east".equalsIgnoreCase(distance[2]);
        "north".equalsIgnoreCase(distance[3]);
        "north".equalsIgnoreCase(distance[4]);
        "west".equalsIgnoreCase(distance[5]);
    }
}
