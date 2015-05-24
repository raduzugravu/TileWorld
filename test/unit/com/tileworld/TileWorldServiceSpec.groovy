package com.tileworld

import com.tileworld.representation.Environment
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(TileWorldService)
class TileWorldServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test initialise"() {

        given:
        String configuration =
            """2 300 10000 4 4 blue green 3 0 0 3
OBSTACLES 1 1 2 1 2 2
TILES 2 blue 2 3
1 green 2 0 1 green 1 3
HOLES 2 green 0 0 2 blue 1 2
            """;

        when:
        Environment environment = service.getConfiguration(configuration);

        then:

        // environment general variables
        environment.numberOfAgents == 2;
        environment.tickTime == 300;
        environment.totalTime == 10000;
        environment.gridHeight == 4;
        environment.gridHeight == 4;

        // agents color
        "blue".equalsIgnoreCase(environment.agents.get(0).color);
        environment.agents.get(0).xPosition == 3;
        environment.agents.get(0).yPosition == 0;
        "green".equalsIgnoreCase(environment.agents.get(1).color);
        environment.agents.get(1).xPosition == 0;
        environment.agents.get(1).yPosition == 3;

        // obstacles
        environment.obstacles.get(0).xPosition == 1;
        environment.obstacles.get(0).yPosition == 1;
        environment.obstacles.get(1).xPosition == 2;
        environment.obstacles.get(1).yPosition == 1;
        environment.obstacles.get(2).xPosition == 2;
        environment.obstacles.get(2).yPosition == 2;

        // tiles
        environment.tiles.get(0).numberOfTiles == 2
        "blue".equalsIgnoreCase(environment.tiles.get(0).color);
        environment.tiles.get(0).xPosition == 2;
        environment.tiles.get(0).yPosition == 3;
        environment.tiles.get(1).numberOfTiles == 1;
        "green".equalsIgnoreCase(environment.tiles.get(1).color);
        environment.tiles.get(1).xPosition == 2;
        environment.tiles.get(1).yPosition == 0;
        environment.tiles.get(2).numberOfTiles == 1
        "green".equalsIgnoreCase(environment.tiles.get(2).color);
        environment.tiles.get(2).xPosition == 1;
        environment.tiles.get(2).yPosition == 3;

        // holes
        environment.holes.get(0).depth == 2
        "green".equalsIgnoreCase(environment.holes.get(0).color);
        environment.holes.get(0).xPosition == 0;
        environment.holes.get(0).yPosition == 0;
        environment.holes.get(1).depth == 2;
        "blue".equalsIgnoreCase(environment.holes.get(1).color);
        environment.holes.get(1).xPosition == 1;
        environment.holes.get(1).yPosition == 2;

        // map
        environment.map[0][0] == 'H';
        environment.map[0][1] == 'E';
        environment.map[0][2] == 'E';
        environment.map[0][3] == 'A';
        environment.map[1][0] == 'E';
        environment.map[1][1] == 'O';
        environment.map[1][2] == 'H';
        environment.map[1][3] == 'T';
        environment.map[2][0] == 'T';
        environment.map[2][1] == 'O';
        environment.map[2][2] == 'O';
        environment.map[2][3] == 'T';
        environment.map[3][0] == 'A';
        environment.map[3][1] == 'E';
        environment.map[3][2] == 'E';
        environment.map[3][3] == 'E';

    }
}
