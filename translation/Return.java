package translation;

class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);  // Disable stack trace to avoid performance hit.
        this.value = value;
    }
}