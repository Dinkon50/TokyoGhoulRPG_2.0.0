package com.tokyoghoul.rpg.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.client.ClientGhoulData;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.UpgradePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ProgressionScreen extends Screen {
    private record Node(String id, String name, String desc, int x, int y, int cost) {}
    private final List<Node> nodes = new ArrayList<>();
    private double zoom = 1.0, panX = 0, panY = 0;
    private double dragX, dragY;
    private boolean dragging;
    private Node hovered;

    public ProgressionScreen(){ super(Component.literal("Древо развития")); }

    @Override protected void init(){ buildTree(); }

    private void buildTree(){
        nodes.clear();
        GhoulData.Race r=ClientGhoulData.race();
        if(r==GhoulData.Race.CCG){
            add("investigation2","РАССЛЕДОВАНИЕ I","Лучше обнаружение целей и следов.",80,120,1);
            add("investigation3","РАССЛЕДОВАНИЕ II","Открывает элитные ветки следователя.",80,250,1);
            add("quinque2","КВИНКЕ I","Усиливает урон и обращение с квинке.",260,120,1);
            add("quinque3","КВИНКЕ II","Открывает продвинутые атаки.",260,250,1);
            add("tactics2","ТАКТИКА I","Контратаки и окно ответного удара.",440,120,1);
            add("tactics3","ТАКТИКА II","Комбо наносят больше урона.",440,250,1);
            add("armor2","БРОНЯ I","Снижает входящий урон.",620,120,1);
            add("armor3","БРОНЯ II","Улучшает стойкость в критическом состоянии.",620,250,1);
            add("discipline2","ДИСЦИПЛИНА I","Стабильность и лечение.",350,390,1);
            add("discipline3","ДИСЦИПЛИНА II","Открывает боевой дух.",350,520,1);
            add("rage","БОЕВОЙ ДУХ","Ульта CCG: скорость, защита и урон.",350,650,4);
        } else if(r==GhoulData.Race.HUMAN){
            add("strength2","БОЕЦ I","Базовая сила и урон.",100,150,1);
            add("strength3","БОЕЦ II","Тяжёлые удары и stagger.",100,300,1);
            add("speed2","МОБИЛЬНОСТЬ I","Быстрее движение и уклонение.",300,150,1);
            add("speed3","МОБИЛЬНОСТЬ II","Открывает продвинутые рывки.",300,300,1);
            add("stealth2","СКРЫТНОСТЬ","Меньше внимания NPC.",500,150,1);
            add("investigation2","РАССЛЕДОВАНИЕ","Поиск следов и целей.",700,150,1);
            add("discipline2","ВЫНОСЛИВОСТЬ","Лучший контроль боя.",500,350,1);
        } else {
            add("kagune2","КАГУНЕ I","Управление формой и базовый урон.",350,100,1);
            add("kagune3","КАГУНЕ II","Дополнительная мощь и дальность.",350,230,1);
            add("kagune4","КАГУНЕ III","Продвинутая форма атаки.",350,360,1);
            add("kagune5","КАГУНЕ IV","Последняя ступень мастерства кагуне.",350,490,1);
            add("strength2","СИЛА I","Тяжёлые удары и stagger.",120,180,1);
            add("strength3","СИЛА II","Усиленные комбо.",120,330,1);
            add("speed2","СКОРОСТЬ I","Рывки и мобильность.",580,180,1);
            add("speed3","СКОРОСТЬ II","Цепочка рывков.",580,330,1);
            add("regen2","РЕГЕНЕРАЦИЯ I","Быстрее восстановление при достаточном RC.",120,480,1);
            add("regen3","РЕГЕНЕРАЦИЯ II","Сильнее восстановление.",120,610,1);
            add("regen4","РЕГЕНЕРАЦИЯ III","Подготовка к Кагуне высокого уровня.",120,740,1);
            add("rage","КРОВАВАЯ ЯРОСТЬ","Гульская ульта.",580,480,5);
            if(r==GhoulData.Race.GHOUL || r==GhoulData.Race.HALF_GHOUL)
                add("kakuja","KAKUJA","Финальная ветка: мощная форма, требует Kagune IV + Regeneration III + уровень 8.",350,780,2);
        }
    }

    private void add(String id,String name,String desc,int x,int y,int cost){nodes.add(new Node(id,name,desc,x,y,cost));}

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float pt){
        renderBackground(g);
        int w=width,h=height;
        g.fill(0,0,w,h,0xE808090C);
        g.fill(0,0,w,52,0xF015151B);
        g.fill(0,h-42,w,h,0xF015151B);
        String race=switch(ClientGhoulData.race()){case GHOUL->"GУЛЬ";case HALF_GHOUL->"ПОЛУГУЛЬ";case CCG->"CCG";default->"ЧЕЛОВЕК";};
        g.drawString(font,"TOKYO GHOUL // ПРОГРЕССИЯ",18,14,0xFFEAEAEA,true);
        g.drawString(font,race+"   УРОВЕНЬ "+ClientGhoulData.level()+"   •   ОЧКИ "+ClientGhoulData.points(),18,31,0xFFE14B5A,false);
        drawTree(g,mouseX,mouseY);
        drawInfo(g,mouseX,mouseY);
        g.drawString(font,"Колесо — масштаб   •   ЛКМ — изучить   •   ПКМ/перетаскивание — двигать дерево   •   ESC — выход",18,h-27,0xFFB8B8C0,false);
        g.drawString(font,"МАСШТАБ "+String.format("%.0f%%",zoom*100),w-110,h-27,0xFF777781,false);
        super.render(g,mouseX,mouseY,pt);
    }

    private void drawTree(GuiGraphics g,int mx,int my){
        hovered=null;
        for(Node n:nodes){
            int sx=screenX(n.x), sy=screenY(n.y);
            for(Node m:nodes){
                if(isConnected(n,m)) drawLine(g,sx,sy,screenX(m.x),screenY(m.y),nodeColor(n,m));
            }
        }
        for(Node n:nodes){
            int sx=screenX(n.x), sy=screenY(n.y), size=(int)(46*zoom);
            boolean inside=mx>=sx-size/2&&mx<=sx+size/2&&my>=sy-size/2&&my<=sy+size/2;
            if(inside) hovered=n;
            boolean learned=learned(n.id); boolean ready=ready(n.id);
            int fill=learned?0xFF6D1623:(ready?0xFF261820:0xFF101116);
            int edge=learned?0xFFFF5366:(ready?0xFFD03A4D:0xFF383A43);
            g.fill(sx-size/2-3,sy-size/2-3,sx+size/2+3,sy+size/2+3,0xFF050507);
            g.fill(sx-size/2,sy-size/2,sx+size/2,sy+size/2,edge);
            g.fill(sx-size/2+3,sy-size/2+3,sx+size/2-3,sy+size/2-3,fill);
            String shortName=n.name.length()>12?n.name.substring(0,12):n.name;
            g.drawCenteredString(font,shortName,sx,sy-4,learned?0xFFFFFFFF:(ready?0xFFE0D4D6:0xFF6E7079));
            g.drawCenteredString(font,"·"+n.cost,sx,sy+9,learned?0xFFFF9AA4:0xFF8A8B94);
        }
    }

    private int nodeColor(Node a,Node b){return learned(a.id)||learned(b.id)?0xFF8F2635:0xFF35363D;}
    private boolean isConnected(Node a,Node b){
        if(a==b) return false;
        String x=a.id,y=b.id;
        return (x.equals("kagune2")&&y.equals("kagune3"))||(x.equals("kagune3")&&y.equals("kagune4"))||(x.equals("kagune4")&&y.equals("kagune5"))||
            (x.equals("kagune5")&&y.equals("kakuja"))||(x.equals("regen4")&&y.equals("kakuja"))||
            (x.equals("strength2")&&y.equals("strength3"))||(x.equals("speed2")&&y.equals("speed3"))||
            (x.equals("regen2")&&y.equals("regen3"))||(x.equals("regen3")&&y.equals("regen4"))||
            (x.equals("investigation2")&&y.equals("investigation3"))||(x.equals("quinque2")&&y.equals("quinque3"))||
            (x.equals("tactics2")&&y.equals("tactics3"))||(x.equals("armor2")&&y.equals("armor3"))||
            (x.equals("discipline2")&&y.equals("discipline3"))||(x.equals("discipline3")&&y.equals("rage"));
    }

    private void drawLine(GuiGraphics g,int x1,int y1,int x2,int y2,int color){
        double dx=x2-x1,dy=y2-y1,len=Math.sqrt(dx*dx+dy*dy),ang=Math.atan2(dy,dx);
        g.pose().pushPose(); g.pose().translate(x1,y1,0); g.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float)ang));
        g.fill(0,-1,(int)len,2,color); g.pose().popPose();
    }

    private void drawInfo(GuiGraphics g,int mx,int my){
        if(hovered==null) return;
        int x=width-300,y=70,w=276,h=150;
        g.fill(x,y,x+w,y+h,0xF0121217); g.fill(x,y,x+w,y+3,0xFFE13D51);
        g.drawString(font,hovered.name,x+14,y+14,0xFFFFFFFF,true);
        int yy=y+36;
        for(String line:font.getSplitter().splitLines(Component.literal(hovered.desc),w-28,font)) {g.drawString(font,line,x+14,yy,0xFFC7C7CD,false);yy+=12;}
        g.drawString(font,"Стоимость: "+hovered.cost+" очк.",x+14,y+h-34,0xFFE14B5A,false);
        g.drawString(font,learned(hovered.id)?"ИЗУЧЕНО":(ready(hovered.id)?"ДОСТУПНО":"ЗАБЛОКИРОВАНО"),x+14,y+h-19,learned(hovered.id)?0xFF65D48C:(ready(hovered.id)?0xFFFFC45C:0xFF777982),true);
    }

    private int screenX(int x){return (int)(width/2 + panX + x*zoom);}
    private int screenY(int y){return (int)(68 + panY + y*zoom);}

    private boolean learned(String id){
        return switch(id){
            case "strength2"->ClientGhoulData.strength()>=2; case "strength3"->ClientGhoulData.strength()>=3;
            case "speed2"->ClientGhoulData.speed()>=2; case "speed3"->ClientGhoulData.speed()>=3;
            case "kagune2"->ClientGhoulData.kagune()>=2; case "kagune3"->ClientGhoulData.kagune()>=3;
            case "kagune4"->ClientGhoulData.kagune()>=4; case "kagune5"->ClientGhoulData.kagune()>=5;
            case "regen2"->ClientGhoulData.regen()>=2; case "regen3"->ClientGhoulData.regen()>=3; case "regen4"->ClientGhoulData.regen()>=4;
            case "stealth2"->ClientGhoulData.stealth()>=2; case "rage"->ClientGhoulData.rageUnlocked(); case "kakuja"->ClientGhoulData.kakujaUnlocked();
            case "investigation2"->ClientGhoulData.investigation()>=2; case "investigation3"->ClientGhoulData.investigation()>=3;
            case "quinque2"->ClientGhoulData.quinque()>=2; case "quinque3"->ClientGhoulData.quinque()>=3;
            case "tactics2"->ClientGhoulData.tactics()>=2; case "tactics3"->ClientGhoulData.tactics()>=3;
            case "armor2"->ClientGhoulData.armor()>=2; case "armor3"->ClientGhoulData.armor()>=3;
            case "discipline2"->ClientGhoulData.discipline()>=2; case "discipline3"->ClientGhoulData.discipline()>=3;
            default->false;};
    }

    private boolean ready(String id){
        if(learned(id)||ClientGhoulData.points()<=0) return false;
        return switch(id){
            case "strength2","speed2","kagune2","regen2","stealth2","investigation2","quinque2","tactics2","armor2","discipline2"->true;
            case "strength3"->ClientGhoulData.strength()>=2; case "speed3"->ClientGhoulData.speed()>=2;
            case "kagune3"->ClientGhoulData.kagune()>=2; case "kagune4"->ClientGhoulData.kagune()>=3; case "kagune5"->ClientGhoulData.kagune()>=4;
            case "regen3"->ClientGhoulData.regen()>=2; case "regen4"->ClientGhoulData.regen()>=3;
            case "investigation3"->ClientGhoulData.investigation()>=2; case "quinque3"->ClientGhoulData.quinque()>=2;
            case "tactics3"->ClientGhoulData.tactics()>=2; case "armor3"->ClientGhoulData.armor()>=2; case "discipline3"->ClientGhoulData.discipline()>=2;
            case "rage"->ClientGhoulData.points()>=(ClientGhoulData.race()==GhoulData.Race.CCG?4:5);
            case "kakuja"->ClientGhoulData.points()>=2&&ClientGhoulData.level()>=8&&ClientGhoulData.kagune()>=5&&ClientGhoulData.regen()>=4;
            default->false;};
    }

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button==0 && hovered!=null && ready(hovered.id)){NetworkHandler.CHANNEL.sendToServer(new UpgradePacket(hovered.id)); return true;}
        if(button==1){dragging=true;dragX=mx;dragY=my;return true;}
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean mouseReleased(double mx,double my,int button){if(button==1)dragging=false;return super.mouseReleased(mx,my,button);}
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){if(dragging){panX+=mx-dragX;panY+=my-dragY;dragX=mx;dragY=my;return true;}return super.mouseDragged(mx,my,button,dx,dy);}
    @Override public boolean mouseScrolled(double mx,double my,double delta){zoom=Math.max(0.65,Math.min(1.45,zoom+(delta>0?0.1:-0.1)));return true;}
}
