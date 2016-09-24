boardDimentions = [45.75,20.75,0];

module bottomHolder() {
    bottomDimentions=[2,3,13]+boardDimentions;
    voltageDivBoard=[28,22+2,13];
    nutPos1 = [bottomDimentions[0]/2-18.5,bottomDimentions[1]/2,0];
    nutPos2 = [bottomDimentions[0]/2+18.5,bottomDimentions[1]/2,0];
    
    difference() {
        cube(bottomDimentions, center=false);
        translate([9+1,0.75-2,1.5]) cube(voltageDivBoard, center=false);
        translate([1,2,8-1.5]) cube(boardDimentions+[0,0,20], center=false);
        translate([bottomDimentions[0]/2,bottomDimentions[1]/2,9]) cylinder(d=5,h=20,center=true,$fn=100);
        translate(nutPos1+[0,0,6.5]) cylinder(d=7, h=10,center=true,$fn=6);
        translate(nutPos2+[0,0,6.5]) cylinder(d=7, h=10,center=true,$fn=6);
        translate(nutPos1) cylinder(d=3.5, h=10,center=true,$fn=100);
        translate(nutPos2) cylinder(d=3.5, h=10,center=true,$fn=100);
    }
}

module topHolder() {
    bottomDimentions=[2.5,3.5,13]+boardDimentions;
    topDimentions = bottomDimentions+[2,2,0];
    usPos1 = [topDimentions[0]/2 - (42-16)/2, topDimentions[1]/2,0];
    usPos2 = [topDimentions[0]/2 + (42-16)/2, topDimentions[1]/2,0];
    // (42-16)2
    
    difference() {
        cube(topDimentions, center=false);
        translate([1,1,1.5]) cube(bottomDimentions, center=false);
        translate(usPos1) cylinder(d=17,h=5,center=true,$fn=100);
        translate(usPos2) cylinder(d=17,h=5,center=true,$fn=100);
    }
}

bottomHolder();
translate([0, -30,0]) topHolder();