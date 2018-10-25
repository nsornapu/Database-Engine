
import java.util.*;
import static java.lang.System.*;

class PageList {

  public RAF raf;
  public ArrayList<Page> pages;

  public void setRaf(RAF r){
    raf = r;
  }

  public void save() throws Exception {
    for(Page p: pages){
      p.save();
    }
  }

  public void add(Page p) throws Exception {
    p.setRaf(raf);
    Page last = pages.get(pages.size()-1);
    last.setRightPointer(p.address());
    pages.add(p);
  }

  public void concat(PageList ps) throws Exception {
    Page first = ps.pages.get(0);
    Page last = pages.get(pages.size()-1);
    last.setRightPointer(first.address());
    this.pages.addAll(ps.pages);
  }

//public static void splitLeftOf(PageList pages,Integer cellIndex) throws Exception {
//  //Defer to splitLeftOf in Page
//}

}
