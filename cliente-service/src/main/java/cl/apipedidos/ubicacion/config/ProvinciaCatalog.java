package cl.apipedidos.ubicacion.config;

final class ProvinciaCatalog {

    private ProvinciaCatalog() {
    }

    static String nombreProvincia(String idProvincia) {
        return switch (idProvincia) {
            case "011" -> "Iquique";
            case "014" -> "Tamarugal";
            case "021" -> "Antofagasta";
            case "022" -> "El Loa";
            case "023" -> "Tocopilla";
            case "031" -> "Copiapó";
            case "032" -> "Chañaral";
            case "033" -> "Huasco";
            case "041" -> "Elqui";
            case "042" -> "Choapa";
            case "043" -> "Limarí";
            case "051" -> "Valparaíso";
            case "052" -> "Isla de Pascua";
            case "053" -> "Los Andes";
            case "054" -> "Petorca";
            case "055" -> "Quillota";
            case "056" -> "San Antonio";
            case "057" -> "San Felipe de Aconcagua";
            case "058" -> "Marga Marga";
            case "061" -> "Cachapoal";
            case "062" -> "Cardenal Caro";
            case "063" -> "Colchagua";
            case "071" -> "Talca";
            case "072" -> "Curicó";
            case "073" -> "Cauquenes";
            case "074" -> "Linares";
            case "081" -> "Concepción";
            case "082" -> "Arauco";
            case "083" -> "Biobío";
            case "091" -> "Cautín";
            case "092" -> "Malleco";
            case "101" -> "Llanquihue";
            case "102" -> "Chiloé";
            case "103" -> "Osorno";
            case "104" -> "Palena";
            case "111" -> "Coyhaique";
            case "112" -> "Aysén";
            case "113" -> "General Carrera";
            case "114" -> "Capitán Prat";
            case "121" -> "Magallanes";
            case "122" -> "Antártica Chilena";
            case "123" -> "Tierra del Fuego";
            case "124" -> "Última Esperanza";
            case "131" -> "Santiago";
            case "132" -> "Cordillera";
            case "133" -> "Chacabuco";
            case "134" -> "Maipo";
            case "135" -> "Melipilla";
            case "136" -> "Talagante";
            case "141" -> "Valdivia";
            case "142" -> "Ranco";
            case "151" -> "Arica";
            case "152" -> "Parinacota";
            case "161" -> "Diguillín";
            case "162" -> "Itata";
            case "163" -> "Punilla";
            default -> throw new IllegalArgumentException("No existe una provincia configurada para el id " + idProvincia);
        };
    }
}