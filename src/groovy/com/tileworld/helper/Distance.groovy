package com.tileworld.helper

/**
 * Created by radu on 27/05/15.
 * 
 * Credits: GREG TROWBRIDGE
 * Source: http://gregtrowbridge.com/a-basic-pathfinding-algorithm/
 * 
 */
class Distance {

    public static def findShortestPath(def startCoordinates, def endCoordinates, def map) {

        def grid = [];
        for(int i = 0; i < map.size(); i++) {
            def row = [];
            for(int j = 0; j < map[i].size(); j++) {
                row.add(map[i][j]);
            }
            grid.add(row);
        }


        def distanceFromTop = startCoordinates[0];
        def distanceFromLeft = startCoordinates[1];

        def list = [];
        def queue = list as Queue;

        def location = [
            distanceFromTop: distanceFromTop,
            distanceFromLeft: distanceFromLeft,
            path: new ArrayList<String>(),
            status: 'Start'
        ];

        queue<<location;

        // Loop through the grid searching for the goal
        while (queue.size() > 0) {
            
            // Take the first location off the queue
            def currentLocation = queue.poll();

            // Explore north
            def newLocation = exploreInDirection(currentLocation, endCoordinates, 'north', grid);
            if ('goal'.equalsIgnoreCase(newLocation.status)) {
                return newLocation.path;
            } else if ('valid'.equalsIgnoreCase(newLocation.status)) {
                queue<<newLocation;
            }

            // Explore east
            newLocation = exploreInDirection(currentLocation, endCoordinates, 'east', grid);
            if ('goal'.equalsIgnoreCase(newLocation.status)) {
                return newLocation.path;
            } else if ('valid'.equalsIgnoreCase(newLocation.status)) {
                queue<<newLocation;
            }

            // Explore south
            newLocation = exploreInDirection(currentLocation, endCoordinates, 'south', grid);
            if ('goal'.equalsIgnoreCase(newLocation.status)) {
                return newLocation.path;
            } else if ('valid'.equalsIgnoreCase(newLocation.status)) {
                queue<<newLocation;
            }

            // Explore west
            newLocation = exploreInDirection(currentLocation, endCoordinates, 'west', grid);
            if ('goal'.equalsIgnoreCase(newLocation.status)) {
                return newLocation.path;
            } else if ('valid'.equalsIgnoreCase(newLocation.status)) {
                queue<<newLocation;
            }
        }

        // No valid path found
        return false;
    }

    private static def exploreInDirection(def currentLocation, def endCoordinates, def direction, def grid) {

        List<String> newPath = new ArrayList<String>();
        currentLocation.path.each { String it ->
            newPath.add(it);
        }
        newPath.add(direction);

        def dft = currentLocation.distanceFromTop;
        def dfl = currentLocation.distanceFromLeft;

        if ('north'.equalsIgnoreCase(direction)) {
            dft -= 1;
        } else if ('east'.equalsIgnoreCase(direction)) {
            dfl += 1;
        } else if ('south'.equalsIgnoreCase(direction)) {
            dft += 1;
        } else if ('west'.equalsIgnoreCase(direction)) {
            dfl -= 1;
        }

        def newLocation = [
            distanceFromTop: dft,
            distanceFromLeft: dfl,
            path: newPath,
            status: 'Unknown'
        ]
        newLocation.status = locationStatus(newLocation, endCoordinates, grid);

        // If this new location is valid, mark it as 'V' (Visited)
        if ('valid'.equalsIgnoreCase(newLocation.status)) {
            grid[newLocation.distanceFromTop][newLocation.distanceFromLeft] = 'V';
        }

        return newLocation;

    }

    private static def locationStatus(def location, def endCoordinates, grid) {

        def gridSize = grid.size();
        def dft = location.distanceFromTop;
        def dfl = location.distanceFromLeft;

        if (location.distanceFromLeft < 0 ||
                location.distanceFromLeft >= gridSize ||
                location.distanceFromTop < 0 ||
                location.distanceFromTop >= gridSize) {
            // location is not on the grid--return false
            return 'invalid';
        } else if (dft == endCoordinates[0] && dfl == endCoordinates[1]) {
            return 'goal';
        } else if ("HAOV".contains(grid[dft][dfl])) {
            // location is either an obstacle or has been visited
            return 'blocked';
        } else {
            return 'valid';
        }

    }

}
