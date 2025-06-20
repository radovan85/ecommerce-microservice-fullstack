import { Customer } from "./customer";
import { ShippingAddress } from "./shipping-address";
import { User } from "./user";

export class RegistrationForm {
    private _user?: User;
    private _customer?: Customer;
    private _shippingAddress?: ShippingAddress;

    get user(): User | undefined {
        return this._user;
    }

    set user(value: User | undefined) {
        this._user = value;
    }

    get customer(): Customer | undefined {
        return this._customer;
    }

    set customer(value: Customer | undefined) {
        this._customer = value;
    }

    get shippingAddress(): ShippingAddress | undefined {
        return this._shippingAddress;
    }

    set shippingAddress(value: ShippingAddress | undefined) {
        this._shippingAddress = value;
    }
}
