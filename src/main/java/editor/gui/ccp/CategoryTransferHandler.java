package editor.gui.ccp;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.gui.editor.EditorFrame;

@SuppressWarnings("serial")
public class CategoryTransferHandler extends TransferHandler
{
    final private EditorFrame editor;
    final private String name;

    public CategoryTransferHandler(EditorFrame f, String n)
    {
        editor = f;
        name = n;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE | TransferHandler.COPY;
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        if (supp.isDrop())
            return false;
        else if (supp.isDataFlavorSupported(DataFlavors.categoryFlavor))
        {
            try
            {
                CategoryTransferData data = (CategoryTransferData)supp.getTransferable().getTransferData(DataFlavors.categoryFlavor);
                return !editor.containsCategory(data.data.getName());
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        return new CategoryTransferData(editor.getCategory(name));
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        try
        {
            if (!canImport(supp) || supp.isDrop())
                return false;
            else
                return editor.addCategory(((CategoryTransferData)supp.getTransferable().getTransferData(DataFlavors.categoryFlavor)).data);
        }
        catch (UnsupportedFlavorException | IOException e)
        {
            return false;
        }
    }

    @Override
    public void exportDone(JComponent source, Transferable data, int action)
    {
        if (action == TransferHandler.MOVE)
            editor.removeCategory(name);
    }
}