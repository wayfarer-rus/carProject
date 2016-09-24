deltaDimentions = [0.5,0.5,1.5];
sonyDCBatteryDimentions = [67,70,22]+deltaDimentions;
wallThickness = 1.5;
caseDimentions = [2*wallThickness,2*wallThickness,wallThickness] + [sonyDCBatteryDimentions[1],sonyDCBatteryDimentions[2],sonyDCBatteryDimentions[0]];

module cutout() {
    //translate([0,0,-1])
    rotate([90,-90,180])
    cube(sonyDCBatteryDimentions+[2,0,0], center=false);
}

module cutoutForFingers() {
    d = sonyDCBatteryDimentions[2];
    h = caseDimentions[0]+2;
    translate([h/2-wallThickness-0.5,d/2,caseDimentions[2]-wallThickness])
    rotate([0,90,0])
    cylinder(d=d, h=h, center=true, $fn=100);
}

module usbPortCutout() {
    size=[wallThickness+1, 6.5, 14];
    translate([-wallThickness-0.5, sonyDCBatteryDimentions[2]/2,sonyDCBatteryDimentions[0]/2-size[2]/2])
    cube(size, center=false);
}

module powerButtonCutout() {
    d = 13;
    h = wallThickness+1;
    pos = [sonyDCBatteryDimentions[1]-18.5,-wallThickness-0.5,sonyDCBatteryDimentions[0]/2];
    translate(pos)
    rotate([90,0,0])
    translate([0,0,-h/2])
    cylinder(d=d,h=h,center=true, $fn=100);
}

module bolt(pos) {
    translate(pos)
    translate([0,0,-1])
    union() {
        linear_extrude(height = 2,center = true, convecity = 10, scale = 2, $fn=100) circle(d=3);
        translate([0,0,-5]) linear_extrude(height = 10,center = true, convecity = 10, $fn=100) circle(d=3);
    }
}

module boltCutout(pos) {
    translate(pos)
    union() {
        rotate([90,0,0])
        bolt();
        rotate([90,0,0])
        translate([0,0,caseDimentions[1]/2])
        cylinder(d=9, h=caseDimentions[1],center=true, $fn=100);
    }
}

module bottomFingerCutout() {
    d = sonyDCBatteryDimentions[2];
    h = 10;
    pos1 = [sonyDCBatteryDimentions[1]/2-(d/2+5),d/2,0];
    pos2 = [sonyDCBatteryDimentions[1]/2+(d/2+5),d/2,0];
    translate(pos1)
    cylinder(d=d,h=h, center=true, $fn=100);
    translate(pos2)
    cylinder(d=d,h=h, center=true, $fn=100);
}

module main() {
    difference() {
        translate([-wallThickness, -wallThickness,-wallThickness])
        cube(caseDimentions, center=false);
        cutout();
        cutoutForFingers();
        usbPortCutout();
        powerButtonCutout();
        pos = [sonyDCBatteryDimentions[1]-12,sonyDCBatteryDimentions[2]-0.01,sonyDCBatteryDimentions[0]-24];
        boltCutout(pos);
        pos2= pos-[50.5,0,21.5];
        boltCutout(pos2);
        bottomFingerCutout();
    }
}

main();
