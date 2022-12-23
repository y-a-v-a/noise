import java.awt.*;

public class SmoothBall extends BufferedApplet {
   int w = 0, h = 0, mx = 0, my = 0, button = 0, pix = 1, F = 8, tx = 0, ty = 0;
   double lx = .65, ly = .65, hx = .35, hy = .35;
   double light [] = {lx,ly,Math.sqrt(1-lx*lx-ly*ly)}, mat[][] = identity();
   double hlight[] = {hx,hy,Math.sqrt(1-hx*hx-hy*hy)};
   static double pp[] = {0,0,0}, p[] = {0,0,0}, d[] = {0,0,0}, dd[] = {0,0,0};
   static int[] D = {0,0,0}, dtmp = {0,0,0};
   boolean isRotating = false;

   public boolean mouseDown(Event e, int x, int y) {
      button = 0;
      if (e.modifiers == Event.ALT_MASK ) button = 1;
      if (e.modifiers == Event.META_MASK) button = 2;
      mx = x;
      my = y;

      x -= w/2;
      y -= h/2;
      isRotating = x*x + y*y < w*w/4;

      return true;
   }
   public boolean mouseDrag(Event e, int x, int y) {
      if (button == 0) {
   if (isRotating)
            mat = mult(mat,mult(rotMat(0,.02*(my-y)), rotMat(1,.02*(mx-x))));
         else {
      tx += x - mx;
      ty += y - my;
         }
         mx = x;
         my = y;
         pix = 2;
         damage = true;
      }
      return true;
   }
   public boolean mouseUp(Event e, int x, int y) {
      switch (button) {
      case 1:
         //ImageIO.write("Test6", bufferImage);
         break;
      case 2:
         F = (F == 8 ? 3 : 8);
         break;
      }
      pix = 1;
      damage = true;
      return true;
   }

// RENDER A FRAME

   public void render(Graphics g) {
      if (!damage)
         return;

// SETUP STUFF, DONE ONLY THE FIRST TIME

      if (w == 0) {
         w = bounds().width;
         h = bounds().height;
         g.setColor(Color.black);
         g.fillRect(0,0,w,h);
      }
      double cPrev = 0, cPrev2 = 0;
      double R = .98*w;

// LOOP THRU ALL SCAN LINES

      for (int y = -h/5 ; y < h+h/2 ; y++) {

// SKIP MOST OF BACKGROUND FOR THIS SCAN LINE

      int r = (int)((w/2)*Math.sqrt(1-4*(y-h/2)*(y-h/2)/R/R));
      int xmin = w/2 - r, xmax = w/2+r;
      for (int x = xmin ; x < xmax ; x++) {

// COMPUTE POINT ON UNIT SPHERE

         int freq = F << 16;
         double scale = (double)w/F;
         double p[] = new double[3];
         p[0] = 2.*(x-(w/2))/R;
         p[1] = 2.*((h/2)-y)/R;
         double rr = p[0]*p[0]+p[1]*p[1];

// WE NEED TO ANTIALIAS NEAR SILHOUETTE

         if (rr > 1)
            continue;
         boolean antialias = rr > 1-10./w;
         boolean near_edge = rr > 1-20./w;

// WHEN TRANSFORMING, ONLY COMPUTE EVERY OTHER PIXEL, EXCEPT NEAR SILHOUETTE
              
         if (!near_edge && pix==2 && (x%2==1 || y%2==1))
               continue;

        p[0] += pix/R;
        p[1] -= pix/R;
        rr = p[0]*p[0]+p[1]*p[1];

        double alpha = antialias ? Math.max(0,Math.min(1,(w/4.)*(1-rr))) : 1;
        p[2] = Math.sqrt(1-rr);

// COMPUTE NOISE IN ROTATED COORDINATE SYSTEM

        xform(pp, mat, p);
  pp[0] -= 2.*tx/R;
  pp[1] += 2.*ty/R;
        int xyz[] = {5000+(int)(freq*pp[0]), 5000+(int)(freq*pp[1]), (int)(freq*pp[2])};
        double c = .5 + .25 * INoise.noise(xyz[0], xyz[1], xyz[2]) / (1 << 16);
        double t = 1 - mult(d,p);
        double normal[] = {t*p[0], t*p[1], t*p[2]};

// DO LIGHTING

        double n2 = mult(normal,normal);
        double lambert = mult(light,p) + mult(light,normal)/n2;
        lambert -= .2 * Math.min(0,lambert);
        double pv = .5 * lambert * lambert;
        double hv = 1.2 * mult(hlight,normal)/n2 * (.5+.5*mult(hlight,p)) - .7;
        hv = (hv > 0) ? hv*hv : 0;

// DO SURFACE COLORING

        double f = .2 * pv + 2.5*hv;
        c = c*c*(3-c-c);
        c = c*c*(3-c-c);
        c = c*c*(3-c-c);
        c = 1-c*c*(3-c-c);
        alpha *= .7 + .3*c;
        g.setColor(new Color(
           clip(alpha*(f + c*.58)),
           clip(alpha*(f + c*.40)),
           clip(alpha*(f + c*.25))
        ));
        int ipix = antialias ? 1 : pix;
        g.fillRect(x,y,ipix,ipix);
      }}
   }

   float clip(double f) { return Math.max(0f,Math.min(1f,(float)f)); }

// GENERAL MATRIX/VECTOR SUPPORT

   double mult(double a[], double b[]) { return a[0]*b[0] + a[1]*b[1] + a[2]*b[2]; }
   double mult(double a[], double b[][], int j) { return a[0]*b[0][j] + a[1]*b[1][j] + a[2]*b[2][j]; }
   double[][] identity() {
      double[][] mat = new double[3][3];
      for (int i = 0 ; i < 3 ; i++)
      for (int j = 0 ; j < 3 ; j++)
         mat[i][j] = (i==j ? 1 : 0);
      return mat;
   }
   double[][] rotMat(int axis, double theta) {
      double[][] mat = identity();
      int a1 = (axis+1) % 3;
      int a2 = (axis+2) % 3;
      mat[a1][a1] =   mat[a2][a2] = Math.cos(theta);
      mat[a1][a2] = -(mat[a2][a1] = Math.sin(theta));
      return mat;
   }
   double[][] mult(double[][] a, double[][] b) {
      double[][] mat = new double[3][3];
      for (int i = 0 ; i < 3 ; i++)
      for (int j = 0 ; j < 3 ; j++)
         mat[i][j] = mult(a[i],b,j);
      return mat;
   }
   void xform(double[] dst, double[][] mat, double[] src) {
      for (int i = 0 ; i < 3 ; i++)
         dst[i] = mult(mat[i],src);
   }
   void ixform(double[] dst, double[][] mat, double[] src) {
      for (int i = 0 ; i < 3 ; i++)
         dst[i] = mult(src,mat,i);
   }
}