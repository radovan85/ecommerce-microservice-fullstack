export class Cart {
    private _cartId?: number;
    private _cartItemsIds?: number[];
    private _cartPrice?: number;

    get cartId(): number | undefined {
        return this._cartId;
    }

    set cartId(value: number | undefined) {
        this._cartId = value;
    }

    get cartItemsIds(): number[] | undefined {
        return this._cartItemsIds;
    }

    set cartItemsIds(value: number[] | undefined) {
        this._cartItemsIds = value;
    }

    get cartPrice(): number | undefined {
        return this._cartPrice;
    }

    set cartPrice(value: number | undefined) {
        this._cartPrice = value;
    }
}
