package eunomia.gui.filter;

import com.vivic.eunomia.filter.FilterEntry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;


public class FilterRenderer implements ListCellRenderer {
    private DefaultListCellRenderer left;
    private JPanel comp;
    
    public FilterRenderer(){
        comp = new JPanel(new BorderLayout());
        left = new DefaultListCellRenderer();

        comp.add(left);

        Font font = new Font("SansSerif", Font.PLAIN, 11);
        Border border = BorderFactory.createEtchedBorder();
        comp.setBorder(border);
        left.setFont(font);
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
        left.setComponentOrientation(list.getComponentOrientation());
        if (isSelected) {
            left.setBackground(list.getSelectionBackground());
            left.setForeground(list.getSelectionForeground());
        } else {
            left.setBackground(list.getBackground());
            left.setForeground(list.getForeground());
        }
        
        if (value != null){
            FilterEntry entry = (FilterEntry)value;
            StringBuilder content = new StringBuilder();
            content.append("<html><body>");
            content.append("<table border=0 width=100%><tr><td>");
            content.append("<i>SRC> </i>");
            if(!entry.isSrcIPSet()){
                content.append("<s>");
            }
            getIPRange(content, entry.getSrc_lip(), entry.getSrc_uip());
            if(!entry.isSrcIPSet()){
                content.append("</s>");
            }
            content.append(":");
            if(!entry.isSrcPortSet()){
                content.append("<s>");
            }
            getPortRange(content, entry.getSrc_lport(), entry.getSrc_uport());
            if(!entry.isSrcPortSet()){
                content.append("</s>");
            }
            content.append("<br>");
            content.append("<i>DST> </i>");
            if(!entry.isDstIPSet()){
                content.append("<s>");
            }
            getIPRange(content, entry.getDst_lip(), entry.getDst_uip());
            if(!entry.isDstIPSet()){
                content.append("</s>");
            }
            content.append(":");
            if(!entry.isDstPortSet()){
                content.append("<s>");
            }
            getPortRange(content, entry.getDst_lport(), entry.getDst_uport());
            if(!entry.isDstPortSet()){
                content.append("</s>");
            }
            content.append("</td></tr></table>");
            content.append("</body></html>");
            comp.setToolTipText("Summary: " + entry.getSpecificSummary());
            left.setText(content.toString());
        }  else {
            left.setText("");
        }
        
        left.setEnabled(list.isEnabled());
        
        return comp;
    }
    
    private String getPortRange(StringBuilder builder, int start, int end){
        if(start != end){
            builder.append("[" + start);
            builder.append(" - ");
            builder.append(end + "]");
        } else {
            builder.append(start + "");
        }
        
        return builder.toString();
    }
    
    private String getIPRange(StringBuilder builder, int[] start, int[] end){
        for (int i = 0; i < start.length; i++){
            int s = start[i];
            int e = end[i];
            
            if(s != e){
                builder.append("[" + s + "-" + e + "]");
            } else {
                builder.append(s + "");
            }
            
            if(i != start.length - 1){
                builder.append(".");
            }
        }
        
        return builder.toString();
    }
}