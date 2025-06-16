export class OrderItem {
    private _orderItemId?: number;
    private _quantity?: number;
    private _price?: number;
    private _productName?: string;
    private _productDiscount?: number;
    private _productPrice?: number;
    private _orderId?: number;

    get orderItemId(): number | undefined {
        return this._orderItemId;
    }

    set orderItemId(value: number | undefined) {
        this._orderItemId = value;
    }

    get quantity(): number | undefined {
        return this._quantity;
    }

    set quantity(value: number | undefined) {
        this._quantity = value;
    }

    get price(): number | undefined {
        return this._price;
    }

    set price(value: number | undefined) {
        this._price = value;
    }

    get productName(): string | undefined {
        return this._productName;
    }

    set productName(value: string | undefined) {
        this._productName = value;
    }

    get productDiscount(): number | undefined {
        return this._productDiscount;
    }

    set productDiscount(value: number | undefined) {
        this._productDiscount = value;
    }

    get productPrice(): number | undefined {
        return this._productPrice;
    }

    set productPrice(value: number | undefined) {
        this._productPrice = value;
    }

    get orderId(): number | undefined {
        return this._orderId;
    }

    set orderId(value: number | undefined) {
        this._orderId = value;
    }
}

