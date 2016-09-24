mainBlockDimensions = [55,38,15];
stepperBoardDimentions = [23,23,5];

module diff() {
    translate([0,0,5]) 
    union() {
        cube([10, -10, 0]+mainBlockDimensions, center = true);
        cube([-10,10,0]+mainBlockDimensions, center = true);
        cube(mainBlockDimensions, center = true);
    }
}

module whole(pos) {
    translate(pos)
    union() {
        cylinder(d=8, h=6.01, center=true, $fn=200);
        cylinder(d=5, h=10.01, center = true, $fn=200);
    }
}

holderDim = [3,3,5] + stepperBoardDimentions;
module stepperHolder(pos) {
    translate(pos)
    difference() {
        translate([-3/2,-3/2,0])
        cube(holderDim);
        translate([0,0,stepperBoardDimentions[2]])
        union(){
            cube(stepperBoardDimentions+[0,0,10]);
            translate([5,-5,0]) cube(stepperBoardDimentions+[-10,10,10]);
            translate([-5,5,0]) cube(stepperBoardDimentions+[10,-10,10]);
        }
    }
}

module wheelsDriverBoardHolder() {
    difference() {
        cube([4,4,0]+mainBlockDimensions, center = true);
        diff();
    }
}

difference() {
    x = mainBlockDimensions[0] + stepperBoardDimentions[0] + 7;
    translate([-x/2 + (mainBlockDimensions[0]+4)/2,0,0])
    union () {
        wheelsDriverBoardHolder();
        pos = [mainBlockDimensions[0]/2+2+1.5,-stepperBoardDimentions[1]/2,-7.5];
        color("red")
        stepperHolder(pos);
    }
    
    whole([20,0,-2.5]);
    whole([-20,0,-2.5]);
}

