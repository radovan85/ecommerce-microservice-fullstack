export class Role {
    private _id?: number;
    private _role?: string;
    private _usersIds?: number[];

    get id(): number | undefined {
        return this._id;
    }

    set id(value: number | undefined) {
        this._id = value;
    }

    get role(): string | undefined {
        return this._role;
    }

    set role(value: string | undefined) {
        this._role = value;
    }

    get usersIds(): number[] | undefined {
        return this._usersIds;
    }

    set usersIds(value: number[] | undefined) {
        this._usersIds = value;
    }
}
