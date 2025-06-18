export class Customer {
    private _customerId?: number;
    private _customerPhone?: string;
    private _shippingAddressId?: number;
    private _userId?: number;
    private _cartId?: number;

    get customerId(): number | undefined {
        return this._customerId;
    }

    set customerId(value: number | undefined) {
        this._customerId = value;
    }

    get customerPhone(): string | undefined {
        return this._customerPhone;
    }

    set customerPhone(value: string | undefined) {
        this._customerPhone = value;
    }

    get shippingAddressId(): number | undefined {
        return this._shippingAddressId;
    }

    set shippingAddressId(value: number | undefined) {
        this._shippingAddressId = value;
    }

    get userId(): number | undefined {
        return this._userId;
    }

    set userId(value: number | undefined) {
        this._userId = value;
    }

    get cartId(): number | undefined {
        return this._cartId;
    }

    set cartId(value: number | undefined) {
        this._cartId = value;
    }
}
