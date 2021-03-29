sys.println("begin script");

minecraft.goToBlock(215, 66, -122);

var items = minecraft.openContainerAt(217, 66, -122);

for (var i = 0; i < items.size(); i++) {
    var item = items[i];
    if (!item.getItem().toString().equals("air")) {
        sys.println(items[i]);
    }
}

sys.println("finished");
