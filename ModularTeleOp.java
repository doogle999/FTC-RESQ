package com.qualcomm.ftcrobotcontroller.opmodes;

import java.util.ArrayList;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

public class ModularTeleOp extends OpMode
{
    private class servoContainer
    {
        private Servo servo;

        private String NAME;

        private double DELTA;
        private double MAX;
        private double MIN;
        private double THRESHOLD;

        private ArrayList<Double> targets = new ArrayList<Double>();

        private double go;

        public servoContainer(String n, double d, double ma, double mi, double th)
        {
            NAME = n;

            DELTA = d;
            MAX = ma;
            MIN = mi;
            THRESHOLD = th;
        }

        public void init()
        {
            servo = hardwareMap.servo.get(NAME);
            go = 0;
        }

        public void loopInit(double ta)
        {
            servo.setPosition(ta);
        }

        public void clearTargets()
        {
            targets.clear();
        }

        public void setTarget(double t, boolean c)
        {
            if(c)
                clearTargets();

            targets.add(Range.clip(t, MIN, MAX));
        }

        public void setPath(double[] ts, int s, boolean c)
        {
            if(c)
                clearTargets();

            for(int i = 0; i < s; i++)
                targets.add(Range.clip(ts[i], MIN, MAX));
        }

        public void update()
        {
            if(!targets.isEmpty())
            {
                if (Math.abs(servo.getPosition() - targets.get(0)) <= THRESHOLD)
                {
                    targets.remove(0);
                }

                if (!targets.isEmpty() && go == 0)
                {
                    if(targets.get(0) > servo.getPosition())
                        servo.setPosition(Range.clip(servo.getPosition() + DELTA, MIN, MAX));
                    else
                        servo.setPosition(Range.clip(servo.getPosition() - DELTA, MIN, MAX));
                }
                go++;
                if(go == 3)
                    go = 0;
            }
        }
    }

    public ModularTeleOp()
    {

    }

    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor backLeft;
    DcMotor backRight;

    DcMotor spinner;

    DcMotor elevator;

    DcMotor winch;

    servoContainer leftClimber = new servoContainer("leftClimber", 0.05, 1, 0, 0.05);
    servoContainer rightClimber = new servoContainer("rightClimber", 0.05, 1, 0, 0.05);
    servoContainer topClimber = new servoContainer("topClimber", 0.05, 1, 0, 0.05);

    servoContainer leftHand = new servoContainer("leftHand", 0.05, 0.75, 0.1, 0.05);
    servoContainer rightHand = new servoContainer("rightHand", 0.05, 0.85, 0.1, 0.05);
    servoContainer centerHand = new servoContainer("centerHand", 0.05, 0.9, 0.4, 0.05);

    servoContainer backShield = new servoContainer("backShield", 0.05, 0.5, 0, 0.05);
    servoContainer frontLeftShield = new servoContainer("frontLeftShield", 0.05, 1, 0.5, 0.05);
    servoContainer frontRightShield = new servoContainer("frontRightShield", 0.05, 0.5, 0, 0.05);

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");

        spinner = hardwareMap.dcMotor.get("spinner");

        elevator = hardwareMap.dcMotor.get("elevator");

        winch = hardwareMap.dcMotor.get("winch");

        leftClimber.init();
        rightClimber.init();
        topClimber.init();

        leftHand.init();
        rightHand.init();
        centerHand.init();

        backShield.init();
        frontRightShield.init();
        frontLeftShield.init();
    }

    boolean initServos = true;

    @Override
    public void loop() {
        // Init Servos
        if (initServos)
        {
            leftClimber.loopInit(1);
            rightClimber.loopInit(0);
            topClimber.loopInit(1);

            leftHand.loopInit(0.1);
            rightHand.loopInit(0.85);
            centerHand.loopInit(0.75);

            backShield.loopInit(0);
            frontLeftShield.loopInit(1);
            frontRightShield.loopInit(0);

            initServos = false;
        }

        // Drive - 1
        frontLeft.setPower(-Range.clip(gamepad1.left_stick_y, -1, 1));
        frontRight.setPower(Range.clip(gamepad1.right_stick_y, -1, 1));
        backLeft.setPower(-Range.clip(gamepad1.left_stick_y, -1, 1));
        backRight.setPower(Range.clip(gamepad1.right_stick_y, -1, 1));

        // Spinner - 2
        if(gamepad2.left_bumper)
            spinner.setPower(1);
        else if(gamepad2.right_bumper)
            spinner.setPower(-1);
        else
            spinner.setPower(0);

        // Elevator - 2
        elevator.setPower(-Range.clip(gamepad2.left_stick_y, -1, 1));

        // Winch - 2
        if(gamepad2.y)
            winch.setPower(1);
        else if(gamepad2.a)
            winch.setPower(-1);
        else
            winch.setPower(0);

        // Left Climber - 1
        if(gamepad1.left_bumper)
            leftClimber.setTarget(1, true);
        else if(gamepad1.left_trigger >= 0.5)
            leftClimber.setTarget(0.4, true);

        // Right Climber - 1
        if(gamepad1.right_bumper)
            rightClimber.setTarget(0, true);
        else if (gamepad1.right_trigger >= 0.5)
            rightClimber.setTarget(0.6, true);

        // Top Climber - 1
        if(gamepad1.y)
            topClimber.setPath(new double[]{1, 0, 1}, 3, true);

        leftClimber.update();
        rightClimber.update();
        topClimber.update();

        // Hand - 2
        rightHand.setTarget(gamepad2.right_trigger * -(0.85 - 0.1) + 0.85, true);
        leftHand.setTarget(gamepad2.left_trigger * (0.75 - 0.1) + 0.1, true);

        centerHand.setTarget(-((gamepad2.right_stick_x + 1) / 4) + 0.9, true);

        leftHand.update();
        rightHand.update();
        centerHand.update();

        // Shields - 1
        if(gamepad1.x)
        {
            backShield.setTarget(0, true);
            frontLeftShield.setTarget(1, true);
            frontRightShield.setTarget(0, true);
        }
        else if (gamepad1.b)
        {
            backShield.setTarget(0.5, true);
            frontLeftShield.setTarget(0.5, true);
            frontRightShield.setTarget(0.5, true);
        }

        backShield.update();
        frontLeftShield.update();
        frontRightShield.update();
    }

    @Override
    public void stop()
    {

    }
}
