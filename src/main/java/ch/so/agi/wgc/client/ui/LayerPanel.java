package ch.so.agi.wgc.client.ui;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.InputType.checkbox;

import org.dominokit.domino.ui.forms.CheckBox;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.icons.Icons;
import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;

import elemental2.core.JsNumber;
import elemental2.core.JsString;
import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;
import jsinterop.base.JsPropertyMap;

public class LayerPanel implements IsElement<HTMLElement>, Attachable {
    private final HTMLElement root;
    private HandlerRegistration handlerRegistration;

    public LayerPanel(JsPropertyMap<?> layer) {
        String name = ((JsString) layer.get("name")).normalize();
        String title = ((JsString) layer.get("title")).normalize();
        double opacity = ((JsNumber) layer.get("opacity")).valueOf();
        Boolean visibility = (Boolean) layer.get("visibility");

        root = div().css("layer-panel").id("layer-panel-"+name).element();
        
        HTMLElement rowElement = Row.create().css("layer-panel-row")
                //.addColumn(Column.span(4).appendChild(input(checkbox).id("toggle-all").style("vertical-align: middle;")))
                .addColumn(Column.span(4).appendChild(CheckBox.create("Filled In").filledIn()))
                .addColumn(Column.span(6).css("layer-panel-title").appendChild(span().textContent(title)))
                .addColumn(Column.span(2).appendChild(Icons.ALL.cancel()))
                .element();
        root.appendChild(rowElement);
    }
    
    @Override
    public void attach(MutationRecord mutationRecord) {
        console.log("attach:" + this);
        console.log("mutationRecord:" + mutationRecord);
    }

    @Override
    public HTMLElement element() {
        return root;
    }
    
    @Override
    public void detach(MutationRecord mutationRecord) {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
        console.log("detach:" + this);
        console.log("mutationRecord:" + mutationRecord);        
    }

}
