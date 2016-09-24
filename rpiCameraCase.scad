module bolt(pos) {
    translate(pos)
    translate([0,0,-1])
    union() {
        linear_extrude(height = 2,center = true, convecity = 10, scale = 2, $fn=100) circle(d=3);
        translate([0,0,-5]) linear_extrude(height = 10,center = true, convecity = 10, $fn=100) circle(d=3);
    }
}

module cornerCutout(pos) {
    translate(pos)
    union() {
        translate([0,0,0.6])
        linear_extrude(height = 0.4, center = false, scale = [1,0], $fn=100) {
            square([3.5,0.5]);
        }
        linear_extrude(height = 0.6, center = false, $fn=100) {
            square([3.5,0.5]);
        }
    }
}

module myCylinder(pos, rad) {
    translate(pos)
    linear_extrude(height = 7, center = false, $fn=100) {
        circle(r=rad, center=true);
    }
}

module caseHook() {
    color("yellow") {
        pos = [0,12,5];
        translate(pos)
        rotate([180,0,180]) cornerCutout([0,0,0]);
    }
}

module caseBack() {
    union() {
        difference() {
            union() {
                color("green")
                linear_extrude(height = 2, center = false, $fn=100) {
                    offset(r = 2) square([26,26], center = true);
                }
                color("red")
                linear_extrude(height = 5, center = false, $fn=100) {
                    difference() {
                        union() {
                            offset(r=2) square([21,20], center = true);
                            translate([0,-12.5/2]) square([25,12.5],center = true);
                        }
                        translate([0,-1])
                        square([20,24], center = true);
                    }
                }
                translate([0,-15+2.5/2]) color("red")
                linear_extrude(height = 3, center = false, $fn=100) {
                    square([17,2.5], center = true);
                }
                translate([-4,0,0]) caseHook();
                mirror([1,0,0]) translate([-4,0,0]) caseHook();
                
            }
            
            translate([0,-9.5,2])
            linear_extrude(height = 3, center = false, $fn=100) {
                square([21,7], center=true);
            }
            
            cut = [-25/2+2,24/2-2,4.5];
            myCylinder(cut,2.4/2);
            mirror([1,0,0]) myCylinder(cut,2.4/2);
            cut2 = cut - [0,12.8,0];
            myCylinder(cut2,2.4/2);
            mirror([1,0,0]) myCylinder(cut2,2.4/2);
            
            p1 = [-15+2.5,-15+2.5,2];
            cornerCutout(p1);
            mirror([1,0,0])
            cornerCutout(p1);
            y1 = 15 - 7.5;
            pos1 = [0,y1,2.01];
            bolt(pos1);
            y2 = y1-15;
            pos2 = [0,y2,2.01];
            bolt(pos2);
        }
        
        color("blue") {
            pos = [-25/2+2,24/2-2,0];
            myCylinder(pos,1.8/2);
            mirror([1,0,0]) myCylinder(pos,1.8/2);
                
            pos1 = pos - [0,12.8,0];
            myCylinder(pos1,1.8/2);
            mirror([1,0,0]) myCylinder(pos1,1.8/2);
        }
    }
}

caseBack();
//translate([15,0,0]) import("raspberri_pi_camera_case_back_v0.4_fixed.STL");
