package com.anton.ninemensmorris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton on 2016-11-28.
 */

public class CustomView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Paint paintRed;
    private Paint paintBlue;
    private Paint paintWhite;
    private Paint paintBlack;
    private Paint paintYellow;
    private Paint paintGreen;
    private Paint paintLightBlue;
    private Paint paintLightRed;
    private NMMGame game;
    private TextView stateText;
    private DrawThread drawThread;

    private int circleRadius;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setGame(NMMGame game){
        if(game!=null){
            this.game = game;
            this.game.prepareTurn();
            drawTextInfo();
        }
    }
    public void setStateText(TextView v){this.stateText = v;}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        circleRadius = (int)(0.02f*(getWidth()+getHeight()));

        drawThread = new DrawThread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        circleRadius = (int)(0.02f*(getWidth()+getHeight()));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread.stopRunning();
        drawThread.interrupt();
    }

    private void init(){
        holder = getHolder();
        holder.addCallback(this);

        paintBlue = new Paint();
        paintBlue.setColor(Color.BLUE);

        paintRed = new Paint();
        paintRed.setColor(Color.RED);

        paintWhite = new Paint();
        paintWhite.setColor(Color.WHITE);

        paintBlack = new Paint();
        paintBlack.setColor(Color.BLACK);

        paintYellow = new Paint();
        paintYellow.setColor(Color.YELLOW);

        paintGreen = new Paint();
        paintGreen.setColor(Color.GREEN);

        paintLightBlue = new Paint();
        paintLightBlue.setARGB(255, 167, 207, 249);
        paintLightBlue.setStyle(Paint.Style.FILL);

        paintLightRed = new Paint();
        paintLightRed.setARGB(255, 249, 179, 179);
        paintLightRed.setStyle(Paint.Style.FILL);
    }

    /**
     * called by drawthread
     * @param canvas
     */
    public void draw(Canvas canvas){

        if(game == null)
            return;

        Node[] nodes = game.getGameboard();
        //Canvas canvas = holder.lockCanvas();

        drawBoard(canvas);

        Paint usePaint = null;
        for(int i = 0; i < nodes.length; i++){
            circleRadius = (int)(0.02f*(getWidth()+getHeight()));
            if(nodes[i].isAnimated() || nodes[i].getChecker() == null){
                usePaint = paintBlack;
                circleRadius = (int)(0.01f*(getWidth()+getHeight()));
            }
            else if(nodes[i].getChecker().getPlayer().equals(NMMGame.PlayerColor.BLUE))
                usePaint = paintBlue;
            else
                usePaint = paintRed;

            canvas.drawCircle(
                    nodes[i].getPosXScaled(getWidth()),
                    nodes[i].getPosYScaled(getHeight()),
                    circleRadius, usePaint);
        }

        if(game.getAnimation() != null){
            AnimatedChecker anim = game.getAnimation();

            if(anim.isDone()){
                game.setAnimation(null);
            }else{
                if(anim.getChecker().getPlayer().equals(NMMGame.PlayerColor.BLUE))
                    usePaint = paintBlue;
                else
                    usePaint = paintRed;

                canvas.drawCircle(anim.getPosXScaled(getWidth()),
                        anim.getPosYScaled(getHeight()),
                        circleRadius, usePaint);
                anim.update();
            }
        }

        if(game.getGameState() == NMMGame.GameStates.MOVE){
            List<Node> available = game.getSelectedNode().getFreeNeighbors();
            usePaint = paintYellow;
            circleRadius = (int)(0.01f*(getWidth()+getHeight()));
            for(int i=0; i< available.size(); i++){
                canvas.drawCircle(
                        available.get(i).getPosXScaled(getWidth()),
                        available.get(i).getPosYScaled((getHeight())),
                        circleRadius, usePaint);
            }
        }


        //holder.unlockCanvasAndPost(canvas);

        //drawTextInfo();
    }
    
    private void drawBoard(Canvas canvas){

        Node[] nodes = game.getGameboard();

        if(game.getTurn().equals(NMMGame.PlayerColor.BLUE))
            canvas.drawRect(0,0,getWidth(), getHeight(), paintLightBlue);
        else
            canvas.drawRect(0,0,getWidth(), getHeight(), paintLightRed);

        paintBlack.setStyle(Paint.Style.STROKE);
        paintBlack.setStrokeWidth(3);


        int h = getHeight();
        int w = getWidth();
        //draw lines
        for(Node n : nodes){
            if(n.getUp() != null)
                canvas.drawLine(n.getPosXScaled(w), n.getPosYScaled(h), n.getUp().getPosXScaled(w), n.getUp().getPosYScaled(h), paintBlack);
            if(n.getRight() != null)
                canvas.drawLine(n.getPosXScaled(w), n.getPosYScaled(h), n.getRight().getPosXScaled(w), n.getRight().getPosYScaled(h), paintBlack);
            if(n.getDown() != null)
                canvas.drawLine(n.getPosXScaled(w), n.getPosYScaled(h), n.getDown().getPosXScaled(w), n.getDown().getPosYScaled(h), paintBlack);
            if(n.getLeft() != null)
                canvas.drawLine(n.getPosXScaled(w), n.getPosYScaled(h), n.getLeft().getPosXScaled(w), n.getLeft().getPosYScaled(h), paintBlack);
        }

        paintBlack.setStyle(Paint.Style.FILL);
    }

    public void drawTextInfo(){
        if(game == null || stateText == null)
            return;

        stateText.setText("");
        if(game.getGameState().equals(NMMGame.GameStates.GAMEOVER)){
            stateText.append("Winner is " + game.getWinner().toString() + "!!!");
        }else{
            stateText.append("\n Red: "+game.getRedCheckers().size() + " \t Blue: " + game.getBlueCheckers().size());
            stateText.append("\n State: " + game.getGameState().toString());
            stateText.append("\n Turn: " + game.getTurn().toString());

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(game!=null)
            game.handleInput(findTouchedNode(event));

        return super.onTouchEvent(event);
    }

    private Node findTouchedNode(MotionEvent event){
        for(Node n : game.getGameboard()){
            if(calcDistance(n.getPosXScaled(getWidth()), n.getPosYScaled(getHeight()),event.getX(), event.getY()) < 1.1f*circleRadius){
                return n;
            }
        }
        return null;
    }

    private float calcDistance(float x1, float y1, float x2, float y2){
        return (float)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
}
