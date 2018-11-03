package cache;

import controllers.OrderController;
import controllers.ProductController;
import model.Order;
import model.Product;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it.        :FIX
public class OrderCache {

    // Opretter en arrayliste hvor ordrer gemmes
    private ArrayList<Order> orders;

    // Tidslængden cache gemmes i
    private long ttl;

    // gemmer hvornår cache er blevet gemt
    private long created;


    public OrderCache() {
        this.ttl = Config.getOrderTtl();
    }

    public ArrayList<Order> getOrders(Boolean forceUpdate) {

        /**
         * Først bliver der tjekket om der skal forceUpdate, dette kan gøres når man vil gemmemtvinge en update af cache
         * Ellers bliver cache opdateret hvis cache er forældet
         * Tilsidst kan en opdatering skyldes at der ikke er noget gemt i cache og derfor må hente cache først
         */
        if (forceUpdate
                || ((this.created + this.ttl) >= (System.currentTimeMillis() / 1000L))
                || this.orders == null) {

            //Henter ordrer fra OrderController, da vi ønsker at opdatere cache
            ArrayList<Order> orders = OrderController.getOrders();

            //Sætter den opdatere arrayliste af ordre som instans, samt giver den et tidsstempel, for at kunne tjekke
            //hvor gammel cache er.
            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        // Returnere cache over ordrer
        return this.orders;

    }
}
