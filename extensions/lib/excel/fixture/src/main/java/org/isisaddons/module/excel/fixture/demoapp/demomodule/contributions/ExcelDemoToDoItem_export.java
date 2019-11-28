package org.isisaddons.module.excel.fixture.demoapp.demomodule.contributions;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.value.Blob;

import org.isisaddons.module.excel.dom.ExcelService;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;

@Mixin(method = "act")
public class ExcelDemoToDoItem_export {

    private final ExcelDemoToDoItem toDoItem;

    public ExcelDemoToDoItem_export(final ExcelDemoToDoItem toDoItem) {
        this.toDoItem = toDoItem;
    }

    @Action
    @ActionLayout(contributed = Contributed.AS_ACTION)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Blob act() {
        throw new UnsupportedOperationException();
//        if(actionInvocationContext.isLast()) {
//            // ie current object only
//            final List toDoItems = actionInvocationContext.getDomainObjects();
//            return excelService.toExcel(toDoItems, ExcelDemoToDoItem.class, ExcelDemoToDoItem.class.getSimpleName(), "toDoItems.xlsx");
//        } else {
//            return null;
//        }
    }

    @Inject ExcelService excelService;
    @Inject ExcelDemoToDoItemMenu excelModuleDemoToDoItems;

}
