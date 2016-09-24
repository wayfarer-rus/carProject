rpiDimentions = [56, 85,3];

module basePlate() {
    translate([-3,0,-3]) cube(rpiDimentions+[6,-19,0]);
}

module screwHolder(pos, d=4+3) {
    translate(pos)
    translate([0,0,1])
    cylinder(h=8, d=d, center = true, $fn = 100);
}

module bolt(pos) {
    translate(pos)
    translate([0,0,-1])
    union() {
        linear_extrude(height = 2,center = true, convecity = 10, scale = 2, $fn=100) circle(d=3);
        translate([0,0,-5]) linear_extrude(height = 10,center = true, convecity = 10, $fn=100) circle(d=3);
    }
}

bottomLeftScrewPos = [rpiDimentions[0]-12.5,5,0];
topRightScrewPos = [-25.5,54.5,0] + bottomLeftScrewPos;
bottomRightBoardSupportPos = [topRightScrewPos[0],bottomLeftScrewPos[1],0];
topLeftBoardSupportPos = [bottomLeftScrewPos[0],topRightScrewPos[1],0];

difference() {
    union() {
        difference() {
            basePlate();
            //bolt([rpiDimentions[0]+3-4.5,5,0]);
            //bolt([-3+4.5,5,0s]);
            x = rpiDimentions[0]+3-4.5;
            bolt([x,62,0]);
            bolt([x-(42-13.5),4,0]);
            //bolt([-3+4.5,56+5,0s]);
            translate([28,33,0]) cube([45,46,40],center=true);
        }

        screwHolder(bottomLeftScrewPos);
        screwHolder(topRightScrewPos);
        screwHolder(bottomRightBoardSupportPos,d=5);
        screwHolder(topLeftBoardSupportPos,d=5);
    }

    translate(bottomLeftScrewPos)
    cylinder(h=10, d=2.7, center = true, $fn = 100);
    translate(topRightScrewPos)
    cylinder(h=10, d=2.7, center = true, $fn = 100);
}