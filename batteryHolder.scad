batteryDimentions = [45,26,17];
holderDimentions = [6,6,6] + batteryDimentions;

module batteryMockup()  {
    cube(batteryDimentions, center = true);
}

module frontCornerHolder(pos) {
    translate(pos)
    rotate([0,-135,0])
    rotate([90,0,0])
    linear_extrude(height = holderDimentions[1]+1, center = true, convexity = 10) polygon(points=[[0,0],[1,0],[0,1]]);
}

module backCornerSlider(pos) {
    translate(pos)
    translate([5,0,0])
    rotate([90,0,180])
    linear_extrude(height = batteryDimentions[1], center = true, convexity = 10) polygon(points=[[0,0],[4,0],[0,2.5]]);
}

module bottomCutout() {
    translate([0,-holderDimentions[1]/2,-10])
    translate([0,5,0])
    cube([30,10,10],center=true);
}

module bolt(pos) {
    translate(pos)
    translate([0,0,-1])
    union() {
        linear_extrude(height = 2,center = true, convecity = 10, scale = 2, $fn=100) circle(d=3);
        translate([0,0,-5]) linear_extrude(height = 10,center = true, convecity = 10, $fn=100) circle(d=3);
    }
}

difference () {
    cube(holderDimentions, center=true);
    
    union() {
        translate([2.5,0,2]) cube([-5,10,4]+batteryDimentions, center=true);
        translate([8,0,5]) cube([0,1,0]+holderDimentions, center=true);
        batteryMockup();
    }
    
    frontCornerHolder([(batteryDimentions[0]/2+sqrt(0.5)),0,-(batteryDimentions[2]/2-sqrt(2)/2)]);
    backCornerSlider([-batteryDimentions[0]/2, 0, batteryDimentions[2]/2]);
    bottomCutout();
    mirror([0,1,0]) bottomCutout();
    
    translate([(5-batteryDimentions[0]/2),0,-batteryDimentions[2]/2])
    rotate([180,0,180])
    backCornerSlider([0,0,0]);
    bolt([19,12,-batteryDimentions[2]/2]);
    bolt([19,0,-batteryDimentions[2]/2]);
}

