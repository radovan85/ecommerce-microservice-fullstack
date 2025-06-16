export class Order {
    private _orderId?: number;
    private _orderPrice?: number;
    private _cartId?: number;
    private _orderedItemsIds?: number[];
    private _addressId?: number;
    private _createTime?: string;

    get orderId(): number | undefined {
        return this._orderId;
    }

    set orderId(value: number | undefined) {
        this._orderId = value;
    }

    get orderPrice(): number | undefined {
        return this._orderPrice;
    }

    set orderPrice(value: number | undefined) {
        this._orderPrice = value;
    }

    get cartId(): number | undefined {
        return this._cartId;
    }

    set cartId(value: number | undefined) {
        this._cartId = value;
    }

    get orderedItemsIds(): number[] | undefined {
        return this._orderedItemsIds;
    }

    set orderedItemsIds(value: number[] | undefined) {
        this._orderedItemsIds = value;
    }

    get addressId(): number | undefined {
        return this._addressId;
    }

    set addressId(value: number | undefined) {
        this._addressId = value;
    }

    get createTime(): string | undefined {
        return this._createTime;
    }

    set createTime(value: string | undefined) {
        this._createTime = value;
    }
}

